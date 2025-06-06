package main.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.regex.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database operations for the library management system, handling user authentication,
 * book searches, and borrowing status.
 */
public class DataBaseManager {
    private static final Logger logger = Logger.getLogger(DataBaseManager.class.getName());
    public final String BOOK_DATABASE_PATH = "main/resources/data/BookData.json";
    public final String USER_DATABASE_PATH = "main/resources/data/UserData.json";

    /**
     * Finds a user in the database based on ID and password.
     * Attempts to decrypt stored user IDs to find a match.
     *
     * @param id The user ID to look for
     * @param password The password to use for decryption
     * @return JsonObject containing user data if found, null otherwise
     */
    public JsonObject findUser(String id, String password) {
        try{
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.warning("No user data found in database file: " + USER_DATABASE_PATH);
                return null;
            }

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = SecurityManager.decrypt(encryptedId, password);
                        if (decryptedId.equals(id)) {
                            return user;
                        }
                    } catch (Exception e) {
                        continue;
                    }

                }
            }
            return null;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to find user: " + id, e);
            return null;
        }
    }

    /**
     * Searches for books in the database based on the provided search term.
     * Matches against book ID, title, author, publisher, and shelf number.
     *
     * @param searchTerm The term to search for (empty string returns all books)
     * @return JsonArray of books that match the search criteria
     */
    public JsonArray findBooks(String searchTerm) {
        logger.fine("Searching books with term: " + searchTerm);
        JsonArray filteredBooks = new JsonArray();
        JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);

        if (bookData != null) {
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();

                String bookID = book.get("BookID").getAsString();
                int shelfNumber = getShelfNumber(bookID);
                String title = book.get("Title").getAsString();
                String author = book.get("Author").getAsString();
                String publisher = book.get("Publisher").getAsString();

                if (searchTerm.isEmpty() || title.toLowerCase().contains(searchTerm.toLowerCase()) || author.toLowerCase().contains(searchTerm.toLowerCase()) || publisher.toLowerCase().contains(searchTerm.toLowerCase()) || bookID.toLowerCase().contains(searchTerm.toLowerCase()) || String.valueOf(shelfNumber).contains(searchTerm)) {
                    filteredBooks.add(book);
                }
            }
            logger.info("Book search completed: found " + filteredBooks.size() + " books matching '" + searchTerm + "'");
        } else {
            logger.severe("Failed to load book database for search operation");
        }
        return filteredBooks;
    }

    /**
     * Finds all books borrowed by a specific user.
     *
     * @param userID The ID of the user
     * @param password The password used for decryption
     * @return JsonArray of books borrowed by the user
     */
    public JsonArray findBorrowedBooks(String userID, String password) {
        JsonArray borrowedBooks = new JsonArray();
        JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);

        if (userData != null) {
            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                if (userData.has(userType)) {
                    JsonArray users = userData.getAsJsonArray(userType);
                    for (int i = 0; i < users.size(); i++) {
                        JsonObject user = users.get(i).getAsJsonObject();
                        String encryptedId = user.get("UserID").getAsString();

                        try {
                            String decryptedId = main.core.SecurityManager.decrypt(encryptedId, password);
                            if (decryptedId.equals(userID)) {
                                if (user.has("Books") && !user.get("Books").isJsonNull()) {
                                    JsonArray books = user.getAsJsonArray("Books");
                                    for (int j = 0; j < books.size(); j++) {
                                        borrowedBooks.add(books.get(j));
                                    }
                                }
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }
        }
        return borrowedBooks;
    }

    /**
     * Gets the title of a book from its ID.
     *
     * @param bookID The ID of the book
     * @return The title of the book, or null if not found
     */
    public String getBookTitle(String bookID) {
        JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);
        if (bookData != null) {
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();
                if (book.get("BookID").getAsString().equals(bookID)) {
                    return book.get("Title").getAsString();
                }
            }
        }
        return null;
    }

    /**
     * Calculates the shelf number from a book ID.
     * The first one or two letters of the book ID represent the shelf code.
     * The shelf number is calculated by converting the letters to numbers (A=1, B=2, etc.)
     * and using a base-26 calculation.
     *
     * @param bookID The ID of the book
     * @return The calculated shelf number, or -1 if invalid format
     */
    public int getShelfNumber(String bookID) {
        Pattern pattern = Pattern.compile("^[A-Z]{1,2}");
        Matcher matcher = pattern.matcher(bookID);

        if (matcher.find()) {
            String shelfCode = matcher.group();
            int shelfNumber = 0;

            for (char c : shelfCode.toCharArray()) {
                int charValue = c - 'A' + 1;
                shelfNumber = shelfNumber * 26 + charValue;
            }
            return shelfNumber;
        }
        return -1;
    }

    /**
     * Calculates the due date for a book based on the issue date and user type.
     * Students get 15 days, General Public gets 7 days.
     *
     * @param issueDate The date the book was issued (in format "YYYY-MM-DD")
     * @param userType The type of user ("Students" or "General Public")
     * @return The due date in "YYYY-MM-DD" format, or null if invalid
     */
    public String getDueDate(String issueDate, String userType) {
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matcher = pattern.matcher(issueDate);

        if (matcher.find()){
            String foundDate = matcher.group();
            String[] dateParts = foundDate.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            LocalDate issueDateObj = LocalDate.of(year, month, day);
            LocalDate dueDate;

            if (userType.equals("Students")) {
                dueDate = issueDateObj.plusDays(15);
            } else if (userType.equals("General Public")) {
                dueDate = issueDateObj.plusDays(7);
            } else {
                return null; // Invalid user type
            }

            return dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    /**
     * Determines the status of a book based on issue date and due date.
     *
     * @param dueDate The date the book is due (in format "YYYY-MM-DD")
     * @return Status code: 1 for on time, 0 for due today, -1 for overdue
     */
    public int getDueStatus(String dueDate) {
        LocalDate dueDateObj = LocalDate.parse(dueDate);
        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDateObj)) {
            return -1; // Book is overdue
        } else if (today.isEqual(dueDateObj)) {
            return 0; // Book is due today
        } else {
            return 1; // Book is not overdue
        }
    }

    /**
     * Calculates the number of days a book is overdue based on issue date and due date.
     *
     * @param dueDate The date the book is due (in format "YYYY-MM-DD")
     * @return Number of days overdue, or 0 if not overdue
     */
    public int getDaysOverdue(String dueDate) {
        LocalDate dueDateObj = LocalDate.parse(dueDate);
        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDateObj)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDateObj, today);
        } else {
            return 0; // Should never be called but just in case...
        }
    }

    /**
     * Determines the user type (Students, General Public, Admins) for a given user ID.
     *
     * @param userId The ID of the user
     * @param password The password for decryption
     * @return The user type as a string, or null if not found
     */
    public String getUserType(String userId, String password) {
        logger.fine("Determining user type for user: " + userId);
        JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);

        if (userData == null) {
            return null;
        }

        for (String userType : new String[]{"Students", "General Public", "Admins"}) {
            JsonArray users = userData.getAsJsonArray(userType);
            for (int i = 0; i < users.size(); i++) {
                JsonObject user = users.get(i).getAsJsonObject();
                String encryptedId = user.get("UserID").getAsString();

                try {
                    String decryptedId = main.core.SecurityManager.decrypt(encryptedId, password);
                    if (decryptedId.equals(userId)) {
                        return userType;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return null;
    }

    public boolean saveUserData(JsonObject userData) {
        return JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);
    }

    /**
     * Updates the due status of a book for a specific user.
     *
     * @param currentUser The ID of the user who borrowed the book
     * @param bookId The ID of the book
     * @param statusActual The new status code: 1 (on time), 0 (due today), -1 (overdue)
     * @param key The encryption key (user's password)
     */
    public void updateDueStatus(String currentUser, String bookId, int statusActual, String key) {
        try {
            JsonManager.saveJsonDueStatus(currentUser, bookId, statusActual, USER_DATABASE_PATH, key);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update due status for user " + currentUser + " book " + bookId, e);
        }
    }

    /**
     * Generates a unique book ID based on the shelf number and book title.
     * The ID consists of a shelf code (A-Z, AA-ZZ) and a six-digit hash of the book title.
     *
     * @param shelfNumber The shelf number as a string
     * @param bookTitle The title of the book
     * @return A unique book ID or null if the shelf number is invalid
     */
    public String generateBookID(String shelfNumber, String bookTitle) {
        logger.fine("Generating book ID for shelf " + shelfNumber + " and title: " + bookTitle);
        try {
            int shelfNum = Integer.parseInt(shelfNumber);
            String shelfCode = generateShelfCode(shelfNum);

            if (shelfCode.isEmpty()) {
                return null;
            }

            int hash = Math.abs(bookTitle.hashCode());
            int sixDigitHash = hash % 1000000;

            String bookId = shelfCode + String.format("%06d", sixDigitHash);
            logger.info("Generated book ID: " + bookId + " for book: " + bookTitle);
            return bookId;

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Failed to generate book ID for shelf " + shelfNumber, e);
            return null;
        }
    }

    /**
     * Generates a shelf code based on the shelf number.
     * The first 26 shelves are represented by A-Z, and the next 676 shelves by AA-ZZ.
     *
     * @param shelfNumber The shelf number (1-based index)
     * @return The shelf code as a string, or null if invalid
     */
    public String generateShelfCode(int shelfNumber) {
        if (shelfNumber <= 0) {
            return null;
        }

        if (shelfNumber <= 26) {
            return String.valueOf((char)('A' + shelfNumber - 1));
        } else if (shelfNumber <= 702) {
            int firstChar = (shelfNumber - 27) / 26 + 'A';
            int secondChar = (shelfNumber - 27) % 26 + 'A';
            return String.valueOf((char)firstChar) + (char)secondChar;
        } else {
            return null;
        }
    }

    /**
     * Returns a book for a specific user and updates both user and book databases.
     * 
     * @param userId The ID of the user returning the book
     * @param bookId The ID of the book being returned
     * @param password The password for decryption
     * @return true if the book was successfully returned, false otherwise
     */
    public boolean returnBook(String userId, String bookId, String password) {
        try {
            if (JsonManager.modifyUserBook(userId, bookId, password, USER_DATABASE_PATH, JsonManager.BookOperation.RETURN)) {
                logger.severe("Failed to remove book " + bookId + " from user " + userId + "'s borrowed list");
                return false;
            }

            if (JsonManager.updateBookAvailability(bookId, BOOK_DATABASE_PATH, JsonManager.BookOperation.RETURN)) {
                logger.warning("Failed to update book availability after return. Book removed from user but availability not updated for: " + bookId);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return book " + bookId + " for user " + userId, e);
            return false;
        }
    }

    /**
     * Finds the book ID based on the book title.
     *
     * @param bookTitle The title of the book to search for
     * @return The book ID if found, null otherwise
     */
    public String findBookID(String bookTitle) {
        JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);
        if (bookData != null) {
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();
                if (book.get("Title").getAsString().equalsIgnoreCase(bookTitle)) {
                    return book.get("BookID").getAsString();
                }
            }
        }
        return null;
    }

    /**
     * Borrows a book for a specific user and updates both user and book databases.
     *
     * @param userId The ID of the user borrowing the book
     * @param shelf The shelf number where the book is located
     * @param bookTitle The title of the book being borrowed
     * @param password The password for decryption
     * @return true if the book was successfully borrowed, false otherwise
     */
    public boolean borrowBook(String userId, String shelf, String bookTitle, String password){
        try {
            String bookId = generateBookID(shelf, bookTitle);

            if (JsonManager.modifyUserBook(userId, bookId, password, USER_DATABASE_PATH, JsonManager.BookOperation.BORROW)) {
                logger.severe("Failed to add book " + bookId + " to user " + userId + "'s borrowed list");
                return false;
            }

            if (JsonManager.updateBookAvailability(bookId, BOOK_DATABASE_PATH, JsonManager.BookOperation.BORROW)) {
                logger.warning("Failed to update book availability after borrow. Book added to user but availability not updated for: " + bookId);
            }

            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to borrow book " + bookTitle + " for user " + userId, e);
            return false;
        }
    }

    /**
     * Checks if a user already has a specific book borrowed.
     *
     * @param userId The ID of the user
     * @param shelf The shelf number where the book is located
     * @param bookTitle The title of the book to check
     * @param password The password for decryption
     * @return true if the user already has the book borrowed, false otherwise
     */
    public boolean hasUserBorrowedBook(String userId, String shelf, String bookTitle, String password) {
        String bookId = generateBookID(shelf, bookTitle);
        logger.fine("Checking if user " + userId + " has already borrowed book " + bookId);
        
        try {
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.warning("No user data found in database file: " + USER_DATABASE_PATH);
                return false;
            }

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = SecurityManager.decrypt(encryptedId, password);
                        if (decryptedId.equals(userId)) {
                            // Found the user, now check their borrowed books
                            if (user.has("Books") && !user.get("Books").isJsonNull()) {
                                JsonArray books = user.getAsJsonArray("Books");
                                for (int j = 0; j < books.size(); j++) {
                                    JsonObject borrowedBook = books.get(j).getAsJsonObject();
                                    String borrowedBookId = borrowedBook.get("BookID").getAsString();
                                    if (borrowedBookId.equals(bookId)) {
                                        logger.info("User " + userId + " already has book " + bookId + " borrowed");
                                        return true;
                                    }
                                }
                            }
                            // User found but doesn't have this book
                            logger.fine("User " + userId + " does not have book " + bookId + " borrowed");
                            return false;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            logger.warning("User " + userId + " not found in database");
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to check if user " + userId + " has borrowed book " + bookId, e);
            return false;
        }
    }
}
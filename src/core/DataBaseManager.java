package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages database operations for the library management system, handling user authentication,
 * book searches, and borrowing status.
 */
@SuppressWarnings("UnnecessaryContinue")
public class DataBaseManager {
    private static final Logger logger = Logger.getLogger(DataBaseManager.class.getName());

    public final String BOOK_DATABASE_PATH = AppDataManager.getDataFilePath("BookData.json");
    public final String USER_DATABASE_PATH = AppDataManager.getDataFilePath("UserData.json");

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
                logger.log(Level.WARNING, "No user data found in database file: " + USER_DATABASE_PATH);
                return null;
            }

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = core.SecurityManager.decrypt(encryptedId, password);
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
        logger.log(Level.FINE, "Searching books with term: " + searchTerm);
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
            logger.log(Level.INFO, "Book search completed: found " + filteredBooks.size() + " books matching '" + searchTerm + "'");
        } else {
            logger.log(Level.SEVERE, "Failed to load book database for search operation");
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
                            String decryptedId = core.SecurityManager.decrypt(encryptedId, password);
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
        logger.log(Level.FINE, "Determining user type for user: " + userId);
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
                    String decryptedId = core.SecurityManager.decrypt(encryptedId, password);
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
        logger.log(Level.FINE, "Generating book ID for shelf " + shelfNumber + " and title: " + bookTitle);
        try {
            int shelfNum = Integer.parseInt(shelfNumber);
            String shelfCode = generateShelfCode(shelfNum);

            if (shelfCode.isEmpty()) {
                return null;
            }

            int hash = Math.abs(bookTitle.hashCode());
            int sixDigitHash = hash % 1000000;

            String bookId = shelfCode + String.format("%06d", sixDigitHash);
            logger.log(Level.INFO, "Generated book ID: " + bookId + " for book: " + bookTitle);
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
                logger.log(Level.SEVERE, "Failed to remove book " + bookId + " from user " + userId + "'s borrowed list");
                return false;
            }

            if (JsonManager.updateBookAvailability(bookId, BOOK_DATABASE_PATH, JsonManager.BookOperation.RETURN)) {
                logger.log(Level.WARNING, "Failed to update book availability after return. Book removed from user but availability not updated for: " + bookId);
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
                logger.log(Level.SEVERE, "Failed to add book " + bookId + " to user " + userId + "'s borrowed list");
                return false;
            }

            if (JsonManager.updateBookAvailability(bookId, BOOK_DATABASE_PATH, JsonManager.BookOperation.BORROW)) {
                logger.log(Level.WARNING, "Failed to update book availability after borrow. Book added to user but availability not updated for: " + bookId);
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
        logger.log(Level.FINE, "Checking if user " + userId + " has already borrowed book " + bookId);
        
        try {
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.log(Level.WARNING, "No user data found in database file: " + USER_DATABASE_PATH);
                return false;
            }

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = core.SecurityManager.decrypt(encryptedId, password);
                        if (decryptedId.equals(userId)) {
                            // Found the user, now check their borrowed books
                            if (user.has("Books") && !user.get("Books").isJsonNull()) {
                                JsonArray books = user.getAsJsonArray("Books");
                                for (int j = 0; j < books.size(); j++) {
                                    JsonObject borrowedBook = books.get(j).getAsJsonObject();
                                    String borrowedBookId = borrowedBook.get("BookID").getAsString();
                                    if (borrowedBookId.equals(bookId)) {
                                        logger.log(Level.INFO, "User " + userId + " already has book " + bookId + " borrowed");
                                        return true;
                                    }
                                }
                            }
                            // User found but doesn't have this book
                            logger.log(Level.FINE, "User " + userId + " does not have book " + bookId + " borrowed");
                            return false;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            logger.log(Level.WARNING, "User " + userId + " not found in database");
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to check if user " + userId + " has borrowed book " + bookId, e);
            return false;
        }
    }

    /**
     * Adds a new book to the database.
     *
     * @param shelfNumber The shelf number where the book will be located
     * @param title The title of the book
     * @param author The author of the book
     * @param publisher The publisher of the book
     * @param available The number of available copies
     * @param onLoan The number of copies currently on loan
     * @return true if the book was successfully added, false otherwise
     */
    public boolean addBook(String shelfNumber, String title, String author, String publisher, int available, int onLoan) {
        try {
            logger.log(Level.INFO, "Adding new book: " + title);

            String bookId = generateBookID(shelfNumber, title);
            if (bookId == null) {
                logger.log(Level.SEVERE, "Failed to generate book ID for shelf " + shelfNumber);
                return false;
            }

            JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);
            if (bookData == null) {
                logger.log(Level.SEVERE, "No book data found in database file: " + BOOK_DATABASE_PATH);
                return false;
            }

            // Check if book already exists
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject existingBook = bookData.get(i).getAsJsonObject();
                if (existingBook.get("BookID").getAsString().equals(bookId)) {
                    logger.log(Level.WARNING, "Book with ID " + bookId + " already exists");
                    return false;
                }
            }

            JsonObject newBook = new JsonObject();
            newBook.addProperty("BookID", bookId);
            newBook.addProperty("Title", title);
            newBook.addProperty("Author", author);
            newBook.addProperty("Publisher", publisher);
            newBook.addProperty("Available", available);
            newBook.addProperty("OnLoan", onLoan);

            bookData.add(newBook);

            boolean success = JsonManager.saveJsonArrayFile(bookData, BOOK_DATABASE_PATH);

            if (success) {
                logger.log(Level.INFO, "Successfully added book: " + title + " with ID: " + bookId);
            } else {
                logger.log(Level.SEVERE, "Failed to save book data after adding: " + title);
            }

            return success;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding book: " + title, e);
            return false;
        }
    }


    /**
     * Updates an existing book in the database.
     *
     * @param originalBookId The original ID of the book to update
     * @param shelfNumber The new shelf number
     * @param title The new title
     * @param author The new author
     * @param publisher The new publisher
     * @param available The new number of available copies
     * @param onLoan The new number of copies on loan
     * @return true if the book was successfully updated, false otherwise
     */
    public boolean updateBook(String originalBookId, String shelfNumber, String title, String author, String publisher, int available, int onLoan) {
        try {
            logger.log(Level.INFO, "Updating book with ID: " + originalBookId);

            String newBookId = generateBookID(shelfNumber, title);
            if (newBookId == null) {
                logger.log(Level.WARNING, "Failed to generate book ID for shelf " + shelfNumber);
                return false;
            }

            JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);
            if (bookData == null) {
                logger.log(Level.SEVERE, "No book data found for update operation");
                return false;
            }

            // Find and update the book
            boolean bookFound = false;
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();
                if (book.get("BookID").getAsString().equals(originalBookId)) {
                    bookFound = true;

                    // If book ID changed due to shelf/title change, check for conflicts
                    if (!newBookId.equals(originalBookId)) {
                        for (int j = 0; j < bookData.size(); j++) {
                            if (j != i) {
                                JsonObject otherBook = bookData.get(j).getAsJsonObject();
                                if (otherBook.get("BookID").getAsString().equals(newBookId)) {
                                    logger.log(Level.WARNING, "Cannot update book: new ID " + newBookId + " already exists");
                                    return false;
                                }
                            }
                        }
                    }

                    book.addProperty("BookID", newBookId);
                    book.addProperty("Title", title);
                    book.addProperty("Author", author);
                    book.addProperty("Publisher", publisher);
                    book.addProperty("Available", available);
                    book.addProperty("OnLoan", onLoan);

                    break;
                }
            }

            if (!bookFound) {
                logger.log(Level.WARNING, "Book with ID " + originalBookId + " not found for update");
                return false;
            }

            // If book ID changed, update user records
            if (!newBookId.equals(originalBookId)) {
                if(updateUserBookReferences(originalBookId, newBookId)) {
                    logger.log(Level.INFO,"User book references updated successfully for book ID change");
                } else {
                    logger.log(Level.SEVERE,"Failed to update user book references for book ID change from " + originalBookId + " to " + newBookId);
                }
            }

            // Save updated data
            boolean success = JsonManager.saveJsonArrayFile(bookData, BOOK_DATABASE_PATH);

            if (success) {
                logger.log(Level.INFO, "Successfully updated book: " + originalBookId + " -> " + newBookId);
            } else {
                logger.log(Level.SEVERE, "Failed to save book data after updating: " + originalBookId);
            }

            return success;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating book: " + originalBookId, e);
            return false;
        }
    }

    /**
     * Updates book ID references in user borrowed books when a book ID changes,
     * or removes book references when a book is deleted.
     * This is necessary when shelf number or title changes, causing a new book ID,
     * or when a book is completely removed from the system.
     *
     * @param oldBookId The old book ID to replace or remove
     * @param newBookId The new book ID to use (null for deletion)
     * @return true if the operation was successful, false otherwise
     */
    private boolean updateUserBookReferences(String oldBookId, String newBookId) {
        boolean isDelete = false;
        try {
            isDelete = (newBookId == null);
            String operationType = isDelete ? "Removing" : "Updating";
            String logMessage = isDelete ?
                    "Removing user book references for deleted book: " + oldBookId :
                    "Updating user book references from " + oldBookId + " to " + newBookId;

            logger.log(Level.INFO, logMessage);

            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.log(Level.WARNING, "No user data found for book reference " + operationType.toLowerCase());
                return false;
            }

            boolean updated = false;

            // Iterate through all user types
            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                if (!userData.has(userType)) continue;

                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();

                    if (user.has("Books") && !user.get("Books").isJsonNull()) {
                        JsonArray books = user.getAsJsonArray("Books");

                        // Iterate backwards to safely remove elements during iteration
                        for (int j = books.size() - 1; j >= 0; j--) {
                            JsonObject book = books.get(j).getAsJsonObject();
                            if (book.get("BookID").getAsString().equals(oldBookId)) {
                                if (isDelete) {
                                    // Remove the book from user's borrowed list
                                    books.remove(j);
                                    updated = true;
                                    logger.log(Level.FINE, "Removed deleted book reference from user in " + userType);
                                } else {
                                    // Update the book ID
                                    book.addProperty("BookID", newBookId);
                                    updated = true;
                                    logger.log(Level.FINE, "Updated book reference for user in " + userType);
                                }
                            }
                        }
                    }
                }
            }

            if (updated) {
                boolean success = JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);
                if (success) {
                    String successMessage = isDelete ?
                            "Successfully removed user book references for deleted book" :
                            "Successfully updated user book references";
                    logger.log(Level.INFO, successMessage);
                    return true;
                } else {
                    logger.log(Level.SEVERE, "Failed to save user data after " + operationType.toLowerCase() + " book references");
                    return false;
                }
            } else {
                String noUpdateMessage = isDelete ?
                        "No user book references found for deleted book ID: " + oldBookId :
                        "No user book references found for old book ID: " + oldBookId;
                logger.log(Level.INFO, noUpdateMessage);
                return true;
            }

        } catch (Exception e) {
            String errorMessage = isDelete ?
                    "Error removing user book references for deleted book: " + oldBookId :
                    "Error updating user book references from " + oldBookId + " to " + newBookId;
            logger.log(Level.SEVERE, errorMessage, e);
            return false;
        }
    }

    /**
     * Deletes a book from the database based on its ID.
     *
     * @param bookId The ID of the book to delete
     * @return true if the book was successfully deleted, false otherwise
     */
    public boolean deleteBook(String bookId) {
        try {
            logger.log(Level.INFO, "Deleting book with ID: " + bookId);

            JsonArray bookData = JsonManager.readJsonArrayFile(BOOK_DATABASE_PATH);
            if (bookData == null) {
                logger.log(Level.SEVERE, "No book data found for delete operation");
                return false;
            }

            // Find and remove the book
            boolean bookFound = false;
            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();
                if (book.get("BookID").getAsString().equals(bookId)) {
                    bookData.remove(i);
                    updateUserBookReferences(bookId, null);
                    bookFound = true;
                    logger.log(Level.INFO, "Book with ID " + bookId + " deleted successfully");
                    break;
                }
            }

            if (!bookFound) {
                logger.log(Level.WARNING, "Book with ID " + bookId + " not found for deletion");
                return false;
            }

            // Save updated data
            boolean success = JsonManager.saveJsonArrayFile(bookData, BOOK_DATABASE_PATH);
            if (success) {
                logger.log(Level.INFO, "Successfully saved updated book database after deletion");
            } else {
                logger.log(Level.SEVERE, "Failed to save book data after deletion");
            }

            // Update user records to remove book references
            if(updateUserBookReferences(bookId, null)) {
                logger.log(Level.INFO,"User book references updated successfully for book deletion");
            } else {
                logger.log(Level.SEVERE,"Failed to update user book references for book deletion with ID: " + bookId);
            }

            return success;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting book with ID: " + bookId, e);
            return false;
        }
    }

    /**
     * Adds a new user to the database.
     *
     * @param userId The ID of the user to add
     * @param userName The name of the user to add
     * @param userPassword The password for the user to add
     * @param userType The type of user (Students, General Public)
     * @return true if the user was successfully added, false otherwise
     */
    public boolean addUser(String userId, String userName, String userPassword, String userType) {
        try {
            logger.log(Level.INFO, "Adding new user: " + userName + " with ID: " + userId);

            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.log(Level.SEVERE, "No user data found in database file: " + USER_DATABASE_PATH);
                return false;
            }

            // Check if user already exists
            JsonObject existingUser = findUser(userId, userPassword);
            if (existingUser != null) {
                logger.log(Level.WARNING, "User with ID " + userId + " already exists");
                return false;
            }

            // Create new user object
            JsonObject newUser = new JsonObject();
            newUser.addProperty("UserID", core.SecurityManager.encrypt(userId, userPassword));
            newUser.addProperty("Name", core.SecurityManager.encrypt(userName, userPassword));
            newUser.addProperty("Password", core.SecurityManager.encrypt(userPassword, userPassword));
            newUser.add("Books", new JsonArray());

            JsonArray userTypeArray = userData.getAsJsonArray(userType);

            if (userTypeArray == null) {
                logger.log(Level.SEVERE, "No user type array found in database file: " + USER_DATABASE_PATH);
                return false;
            }

            userTypeArray.add(newUser);

            // Save updated data
            return JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);


        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding user: " + userName, e);
            return false;
        }
    }

    /**
     * Removes a user from the database.
     *
     * @param userId The ID of the user to remove
     * @param password The user's password for verification
     * @return true if the user was successfully removed, false otherwise
     */
    public boolean removeUser(String userId, String password) {
        try {
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.log(Level.SEVERE, "Failed to read user database");
                return false;
            }

            // Find and remove user from appropriate category
            for (String userType : new String[]{"Students", "General Public"}) {
                JsonArray userArray = userData.getAsJsonArray(userType);
                if (userArray != null) {
                    for (int i = 0; i < userArray.size(); i++) {
                        JsonObject user = userArray.get(i).getAsJsonObject();
                        try {
                            String decryptedUserId = core.SecurityManager.decrypt(
                                    user.get("UserID").getAsString(), password);
                            if (decryptedUserId.equals(userId)) {
                                userArray.remove(i);
                                return JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);
                            }
                        } catch (Exception e) {
                            // Continue to next user if decryption fails
                            continue;
                        }
                    }
                }
            }

            logger.log(Level.WARNING, "User not found for removal: " + userId);
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to remove user from database: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateUser(String originalUserId, String userName, String userPassword, String userType) {
        try {
            logger.log(Level.INFO, "Updating user with ID: " + originalUserId);

            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                logger.log(Level.SEVERE, "No user data found for update operation");
                return false;
            }

            // Map user type to database key
            String newTypeKey = UserTypeMapper.mapToCanonical(userType);

            // Find and update the user
            for (String currentType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(currentType);
                if (users != null) {
                    for (int i = 0; i < users.size(); i++) {
                        JsonObject user = users.get(i).getAsJsonObject();
                        try {
                            String decryptedUserId = core.SecurityManager.decrypt(
                                    user.get("UserID").getAsString(), userPassword);

                            if (decryptedUserId.equals(originalUserId)) {
                                // Update user details
                                user.addProperty("UserID", SecurityManager.encrypt(originalUserId, userPassword));
                                user.addProperty("Name", core.SecurityManager.encrypt(userName, userPassword));
                                user.addProperty("Password", core.SecurityManager.encrypt(userPassword, userPassword));

                                // Handle type change if necessary
                                if (!currentType.equals(newTypeKey)) {
                                    if (!moveUserToNewType(userData, user, currentType, newTypeKey)) {
                                        logger.log(Level.SEVERE, "Failed to move user to new type: " + newTypeKey);
                                        return false;
                                    }
                                }

                                // Save and return
                                boolean success = JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);
                                if (success) {
                                    logger.log(Level.INFO, "Successfully updated user: " + originalUserId);
                                } else {
                                    logger.log(Level.SEVERE, "Failed to save user data after update");
                                }
                                return success;
                            }
                        } catch (Exception e) {
                            logger.log(Level.FINE, "Failed to decrypt user ID during search", e);
                            continue;
                        }
                    }
                }
            }

            // User not found
            logger.log(Level.WARNING, "User with ID " + originalUserId + " not found for update");
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating user: " + originalUserId, e);
            return false;
        }
    }

    /**
     * Moves a user from one type category to another.
     *
     * @param userData The complete user data JSON object
     * @param user The user object to move
     * @param oldType The old user type category
     * @param newType The new user type category
     * @return true if the move was successful, false otherwise
     */
    private boolean moveUserToNewType(JsonObject userData, JsonObject user, String oldType, String newType) {
        try {
            // Remove from old type
            JsonArray oldTypeArray = userData.getAsJsonArray(oldType);
            if (oldTypeArray != null) {
                for (int i = 0; i < oldTypeArray.size(); i++) {
                    if (oldTypeArray.get(i).getAsJsonObject().equals(user)) {
                        oldTypeArray.remove(i);
                        break;
                    }
                }
            }

            // Add to new type
            JsonArray newTypeArray = userData.getAsJsonArray(newType);
            if (newTypeArray == null) {
                newTypeArray = new JsonArray();
                userData.add(newType, newTypeArray);
            }
            newTypeArray.add(user);

            logger.log(Level.INFO, "Successfully moved user from " + oldType + " to " + newType);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error moving user between types", e);
            return false;
        }
    }

}
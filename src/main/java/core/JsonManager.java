package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

/**
 * Utility class for handling JSON file operations in the library management system.
 * Provides methods for reading and writing JSON objects and arrays to files.
 */
@SuppressWarnings("UnnecessaryContinue")
public class JsonManager {
    private static final Logger logger = Logger.getLogger(JsonManager.class.getName());

    /**
     * Determines if a path is an absolute file path or just a filename.
     * @param path The path to check
     * @return true if the path is absolute, false if it's just a filename
     */
    private static boolean isAbsolutePath(String path) {
        return path.contains(File.separator) || path.contains("/");
    }

    /**
     * Gets the appropriate file path, either using the path as-is if it's absolute,
     * or prepending the data directory if it's just a filename.
     * @param path The path or filename
     * @return The full file path
     */
    private static String getProperFilePath(String path) {
        if (isAbsolutePath(path)) {
            return path; // Already a full path
        } else {
            return AppDataManager.getDataFilePath(path); // Just a filename
        }
    }

    /**
     * Reads a JSON file from the user data directory or from a specified path.
     */
    public static JsonObject readJsonFile(String pathOrFileName) {
        String filePath = getProperFilePath(pathOrFileName);
        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            logger.log(Level.FINE, "Successfully read JSON object from: " + filePath);
            return jsonObject;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON file: " + filePath, e);
            return null;
        }
    }

    /**
     * Reads a JSON array file from the user data directory or from a specified path.
     */
    public static JsonArray readJsonArrayFile(String pathOrFileName) {
        String filePath = getProperFilePath(pathOrFileName);
        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            logger.log(Level.FINE, "Successfully read JSON array from: " + filePath);
            return jsonArray;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON array file: " + filePath, e);
            return null;
        }
    }

    /**
     * Saves a JsonObject to the user data directory or to a specified path.
     */
    public static boolean saveJsonFile(JsonObject jsonObject, String pathOrFileName) {
        String filePath = getProperFilePath(pathOrFileName);
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(jsonObject);
            
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(jsonString);
            }
            
            logger.log(Level.FINE, "Successfully saved JSON object to: " + filePath);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving JSON object to: " + filePath, e);
            return false;
        }
    }

    /**
     * Saves a JsonArray to the user data directory or to a specified path.
     */
    public static boolean saveJsonArrayFile(JsonArray jsonArray, String pathOrFileName) {
        String filePath = getProperFilePath(pathOrFileName);
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(jsonArray);
            
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(jsonString);
            }
            
            logger.log(Level.FINE, "Successfully saved JSON array to: " + filePath);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving JSON array to: " + filePath, e);
            return false;
        }
    }

    /**
     * Updates the due status of a borrowed book for a specific user in the database.
     * Finds the user by decrypting user IDs, then locates and updates the specific book's status.
     *
     * @param currentUser The ID of the user who borrowed the book
     * @param bookId The ID of the book whose status needs to be updated
     * @param statusActual The new status code (1: on time, 0: due today, -1: overdue)
     * @param userDatabasePath Path to the user database file
     * @param key The encryption key (user's password) for decrypting user IDs
     */
    public static void saveJsonDueStatus(String currentUser, String bookId, int statusActual, String userDatabasePath, String key) {
        try {
            JsonObject userData = JsonManager.readJsonFile(userDatabasePath);
            if (userData == null) {
                logger.log(Level.SEVERE, "User data is null. Cannot update due status.");
                return;
            }

            boolean updated = false;
            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                if (!userData.has(userType)) continue;

                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = SecurityManager.decrypt(encryptedId, key);
                        if (decryptedId.equals(currentUser)) {
                            if (user.has("Books") && !user.get("Books").isJsonNull()) {
                                JsonArray books = user.getAsJsonArray("Books");
                                for (int j = 0; j < books.size(); j++) {
                                    JsonObject book = books.get(j).getAsJsonObject();
                                    String currentBookId = book.get("BookID").getAsString();

                                    if (currentBookId.equals(bookId)) {
                                        book.addProperty("Status", statusActual);
                                        updated = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (updated) break;

                    } catch (Exception e) {
                        continue;
                    }
                }

                if (updated) break;

            }

            if (updated) {
                saveJsonFile(userData, userDatabasePath);
                logger.log(Level.INFO, "Successfully updated due status for user: " + currentUser);
            } else {
                logger.log(Level.WARNING, "User not found or book not found for user: " + currentUser);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating due status for user: " + currentUser, e);
        }
    }
    
    public enum BookOperation {
        BORROW,
        RETURN
    }

    /**
     * Updates book availability counts when a book is borrowed or returned.
     *
     * @param bookId The ID of the book
     * @param bookDatabasePath Path to the book database file
     * @param operation The type of operation (BORROW or RETURN)
     * @return true if successful, false otherwise
     */
    public static boolean updateBookAvailability(String bookId, String bookDatabasePath, BookOperation operation) {
        try {
            JsonArray bookData = readJsonArrayFile(bookDatabasePath);
            if (bookData == null) {
                logger.log(Level.SEVERE, "Book data is null. Cannot update availability.");
                return true;
            }

            for (int i = 0; i < bookData.size(); i++) {
                JsonObject book = bookData.get(i).getAsJsonObject();
                if (book.get("BookID").getAsString().equals(bookId)) {
                    int available = book.get("Available").getAsInt();
                    int onLoan = book.get("OnLoan").getAsInt();

                    switch (operation) {
                        case BORROW:
                            if (available <= 0) {
                                logger.log(Level.WARNING, "Book not available for borrowing: " + bookId);
                                return true;
                            }
                            book.addProperty("Available", available - 1);
                            book.addProperty("OnLoan", onLoan + 1);
                            break;

                        case RETURN:
                            book.addProperty("Available", available + 1);
                            book.addProperty("OnLoan", Math.max(0, onLoan - 1));
                            break;
                    }
                    
                    boolean success = saveJsonArrayFile(bookData, bookDatabasePath);
                    
                    if (success) {
                        logger.log(Level.INFO, "Successfully updated book availability for " + operation + ": " + bookId);
                    } else {
                        logger.log(Level.SEVERE, "Failed to save book data after availability update");
                    }
                    return !success;
                }
            }

            logger.log(Level.WARNING, "Book not found: " + bookId);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating book availability for " + operation + ": " + bookId, e);
            return true;
        }
    }


    /**
     * Removes a book from a user's borrowed books list.
     * 
     * @param userId The ID of the user
     * @param bookId The ID of the book to remove
     * @param password The encryption password
     * @param userDatabasePath Path to the user database file
     * @param operation The type of operation (BORROW or RETURN)
     * @return true if successful, false otherwise
     */
    public static boolean modifyUserBook(String userId, String bookId, String password, String userDatabasePath, BookOperation operation) {
        try {
            JsonObject userData = readJsonFile(userDatabasePath);
            if (userData == null) {
                logger.log(Level.SEVERE, "User data is null. Cannot modify book.");
                return true;
            }

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {
                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = SecurityManager.decrypt(encryptedId, password);
                        if (decryptedId.equals(userId)) {

                            switch (operation) {
                                case BORROW:
                                    if (!user.has("Books")) {
                                        user.add("Books", new JsonArray());
                                    }

                                    JsonArray borrowBooks = user.getAsJsonArray("Books");

                                    JsonObject newBook = new JsonObject();
                                    newBook.addProperty("BookID", bookId);

                                    String currentDate = java.time.LocalDate.now().toString();
                                    String encryptedDate = SecurityManager.encrypt(currentDate, password);
                                    newBook.addProperty("DateIssued", encryptedDate);

                                    newBook.addProperty("Status", 1);

                                    borrowBooks.add(newBook);

                                    // SAVE AND RETURN
                                    boolean borrowSuccess = saveJsonFile(userData, userDatabasePath);
                                    if (borrowSuccess) {
                                        logger.log(Level.INFO, "Successfully added book " + bookId + " to user " + userId);
                                    }
                                    return !borrowSuccess;
                                    
                                case RETURN:
                                    if (user.has("Books") && !user.get("Books").isJsonNull()) {
                                        JsonArray returnBooks = user.getAsJsonArray("Books");
                                        for (int j = 0; j < returnBooks.size(); j++) {
                                            JsonObject book = returnBooks.get(j).getAsJsonObject();
                                            if (book.get("BookID").getAsString().equals(bookId)) {
                                                returnBooks.remove(j);
                                                
                                                // SAVE AND RETURN
                                                boolean returnSuccess = saveJsonFile(userData, userDatabasePath);
                                                if (returnSuccess) {
                                                    logger.log(Level.INFO, "Successfully removed book " + bookId + " from user " + userId);
                                                }
                                                return !returnSuccess;
                                            }
                                        }
                                    }
                                    // User found but book not found for removal
                                    logger.log(Level.WARNING, "Book " + bookId + " not found in user " + userId + "'s borrowed list");
                                    return true;
                            }
                        }
                    } catch (Exception _) {
                        continue; // Wrong password, try next user
                    }
                }
            }

            return true; // User not found

        } catch (Exception e) {
            String operationStr = (operation == BookOperation.BORROW) ? "adding" : "removing";
            logger.log(Level.SEVERE, "Error " + operationStr + " book for user: " + userId, e);
            return true;
        }
    }

}
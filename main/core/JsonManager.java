package main.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling JSON file operations in the library management system.
 * Provides methods for reading and writing JSON objects and arrays to files.
 */
public class JsonManager {
    private static final Logger logger = Logger.getLogger(JsonManager.class.getName());

    /**
     * Reads a JSON file and returns it as a JsonObject.
     *
     * @param filePath Path to the JSON file to be read
     * @return JsonObject representation of the file content, or null if an error occurs
     */
    public static JsonObject readJsonFile(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON file: " + filePath, e);
            return null;
        }
    }

    /**
     * Reads a JSON file and returns it as a JsonArray.
     *
     * @param filePath Path to the JSON file to be read
     * @return JsonArray representation of the file content, or null if an error occurs
     */
    public static JsonArray readJsonArrayFile(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON array file: " + filePath, e);
            return null;
        }
    }

    /**
     * Saves a JsonObject to a file with pretty printing.
     *
     * @param jsonObject The JsonObject to be saved
     * @param filePath Path where the JSON file should be saved
     * @return true if the operation was successful, false otherwise
     */
    public static boolean saveJsonFile(JsonObject jsonObject, String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
            logger.log(Level.INFO, "Successfully wrote JSON to file: " + filePath);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing JSON file: " + filePath, e);
            return false;
        }
    }

    /**
     * Saves a JsonArray to a file with pretty printing.
     *
     * @param jsonArray The JsonArray to be saved
     * @param filePath Path where the JSON file should be saved
     * @return true if the operation was successful, false otherwise
     */
    public static boolean saveJsonArrayFile(JsonArray jsonArray, String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonArray, writer);
            logger.log(Level.INFO, "Successfully wrote JSON array to file: " + filePath);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing JSON array file: " + filePath, e);
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
                if (userData.has(userType)) continue;

                JsonArray users = userData.getAsJsonArray(userType);
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String encryptedId = user.get("UserID").getAsString();

                    try {
                        String decryptedId = main.core.SecurityManager.decrypt(encryptedId, key);
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
}
package Core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.regex.*;

public class DataBaseManager {
    //! SET FINAL PATHS BEFORE FINISHING PROJECT

    public final String BOOK_DATABASE_PATH = "DATA/BookData.json";
    public final String USER_DATABASE_PATH = "DATA/UserData.json";

    public JsonObject findUser(String id, String password) {
        try{
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);
            if (userData == null) {
                System.out.println("No user data found.");
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
            System.err.println("Failed to find user: " + e.getMessage());
            return null;
        }
    }

    public JsonArray findBooks(String searchTerm) {
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
        }
        return filteredBooks;
    }

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

    //! Only used for testing remove before finishing project
    public void encryptAllData() {
        try {
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {

                JsonArray users;
                if (userData != null) {
                    users = userData.getAsJsonArray(userType);
                } else {
                    System.out.println("No user data found for type: " + userType);
                    continue;
                }

                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String password = user.get("Password").getAsString();

                    // Encrypt the name
                    String name = user.get("Name").getAsString();
                    String encryptedName = SecurityManager.encrypt(name, password);
                    user.addProperty("Name", encryptedName);

                    // Encrypt the user ID
                    String userId = user.get("UserID").getAsString();
                    String encryptedUserId = SecurityManager.encrypt(userId, password);
                    user.addProperty("UserID", encryptedUserId);

                    // If this user has books, encrypt the date issued
                    if (user.has("Books") && !user.get("Books").isJsonNull()) {
                        JsonArray books = user.getAsJsonArray("Books");
                        for (int j = 0; j < books.size(); j++) {
                            JsonObject book = books.get(j).getAsJsonObject();
                            String dateIssued = book.get("DateIssued").getAsString();
                            String encryptedDate = SecurityManager.encrypt(dateIssued, password);
                            book.addProperty("DateIssued", encryptedDate);
                        }
                    }

                    // Encrypt password
                    String encryptedPassword = SecurityManager.encrypt(password, password);
                    user.addProperty("Password", encryptedPassword);
                }
            }

            boolean userDataSaved = saveUserData(userData);
            if (!userDataSaved) {
                System.out.println("Failed to save user data.");
                return;
            }

            System.out.println("User data encrypted successfully.");
        } catch (Exception e) {
            System.err.println("Failed to encrypt user data: " + e.getMessage());
        }
    }

    //! Only used for testing remove before finishing project
    public void decryptAllData() {
        try {
            JsonObject userData = JsonManager.readJsonFile(USER_DATABASE_PATH);

            for (String userType : new String[]{"Students", "General Public", "Admins"}) {

                JsonArray users;
                if (userData != null) {
                    users = userData.getAsJsonArray(userType);
                } else {
                    System.out.println("No user data found for type: " + userType);
                    continue;
                }

                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i).getAsJsonObject();
                    String password = user.get("Password").getAsString();

                    // Encrypt the name
                    String name = user.get("Name").getAsString();
                    String decryptedName = SecurityManager.decrypt(name, password);
                    user.addProperty("Name", decryptedName);

                    // If this user has books, encrypt the date issued
                    if (user.has("Books") && !user.get("Books").isJsonNull()) {
                        JsonArray books = user.getAsJsonArray("Books");
                        for (int j = 0; j < books.size(); j++) {
                            JsonObject book = books.get(j).getAsJsonObject();
                            String dateIssued = book.get("DateIssued").getAsString();
                            String decryptedDate = SecurityManager.decrypt(dateIssued, password);
                            book.addProperty("DateIssued", decryptedDate);
                        }
                    }
                }
            }

            boolean userDataSaved = saveUserData(userData);
            if (!userDataSaved) {
                System.out.println("Failed to save user data.");
                return;
            }

            System.out.println("User data decrypted successfully.");
        } catch (Exception e) {
            System.err.println("Failed to decrypted user data: " + e.getMessage());
        }
    }

    public boolean saveUserData(JsonObject userData) {
        return JsonManager.saveJsonFile(userData, USER_DATABASE_PATH);
    }
}
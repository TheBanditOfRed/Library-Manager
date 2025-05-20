package Core;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonManager {
    private static final Logger logger = Logger.getLogger(JsonManager.class.getName());

    public static JsonObject readJsonFile(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON file: " + filePath, e);
            return null;
        }
    }

    public static JsonArray readJsonArrayFile(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading JSON array file: " + filePath, e);
            return null;
        }
    }

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
}
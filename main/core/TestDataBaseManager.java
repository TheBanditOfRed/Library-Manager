package main.core;

import java.io.File;

public class TestDataBaseManager {
    public static void main(String[] args) {

        File file = new File("data/BookData.json");
        if (file.exists()) {
            System.out.println("Found JSON file: " + file.getAbsolutePath());
        } else {
            System.out.println("ERROR: JSON file not found at: " + file.getAbsolutePath());
            return;
        }

        // Then test the database manager
        DataBaseManager dbm = new DataBaseManager();
        dbm.encryptAllData();
    }
}
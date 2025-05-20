package main.core;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceManager {
    private static ResourceBundle resourceBundle;
    private static Locale currentLocale;

    static {
        try {
            setLocale(Locale.getDefault());
            System.out.println("Locale set to: " + currentLocale);
        } catch (Exception e) {
            setLocale(Locale.ENGLISH);
            System.out.println("Error setting locale, defaulting to English: " + e.getMessage());
        }
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle("main.resources.lang.messages", locale);
    }

    public static void setLocale(String languageTag) {
        if (languageTag.length() == 2) {
            setLocale(Locale.forLanguageTag(languageTag));

        } else if (languageTag.contains("_")) {
            String bcp47Tag = languageTag.replace('_', '-');
            setLocale(Locale.forLanguageTag(bcp47Tag));

        } else {
            setLocale(Locale.forLanguageTag(languageTag));
        }
    }

    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return "Missing translation for key: " + key;
        }
    }

    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(resourceBundle.getString(key), params);
        } catch (Exception e) {
            return "Missing translation for key: " + key;
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}

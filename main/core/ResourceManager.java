package main.core;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages internationalization resources for the library management system.
 * Provides methods for setting the application locale and retrieving localized strings.
 * Supports language switching at runtime and parameterized message formatting.
 */
public class ResourceManager {
    private static final Logger logger = Logger.getLogger(ResourceManager.class.getName());
    private static ResourceBundle resourceBundle;
    private static Locale currentLocale;

    static {
        try {
            setLocale(Locale.getDefault());
            logger.info("Locale set to: " + currentLocale);
        } catch (Exception e) {
            setLocale(Locale.ENGLISH);
            logger.log(Level.SEVERE, "Failed to set default locale, using English", e);
        }
    }

    /**
     * Sets the application locale using a Locale object.
     * Updates the resource bundle to use the specified locale.
     *
     * @param locale The Locale object to set as the current locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle("main.resources.lang.messages", locale);
    }

    /**
     * Sets the application locale using a language tag string.
     * Handles various formats: two-letter codes (e.g., "en"),
     * Java locale format with underscores (e.g., "en_US"),
     * and BCP 47 language tags (e.g., "en-US").
     *
     * @param languageTag A string representing the language to use
     */
    public static void setLocale(String languageTag) {
        logger.info("Changing application locale to: " + languageTag);
        try {
            if (languageTag.length() == 2) {
                setLocale(Locale.forLanguageTag(languageTag));

            } else if (languageTag.contains("_")) {
                String bcp47Tag = languageTag.replace('_', '-');
                setLocale(Locale.forLanguageTag(bcp47Tag));

            } else {
                setLocale(Locale.forLanguageTag(languageTag));
            }
            logger.info("Successfully changed locale to: " + languageTag);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to change locale to: " + languageTag, e);
        }
    }

    /**
     * Retrieves a localized string for the given key.
     * If the key is not found, returns an error message containing the missing key.
     *
     * @param key The resource key to look up
     * @return The localized string value or an error message if the key wasn't found
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            logger.warning("Missing resource key: " + key + " for locale: " + currentLocale);
            return "Missing translation for key: " + key;
        }
    }

    /**
     * Retrieves a localized string and formats it with the provided parameters.
     * Uses MessageFormat to substitute parameters into placeholders in the string.
     *
     * @param key The resource key to look up
     * @param params Variable argument list of parameters to be inserted into the message
     * @return The formatted localized string or an error message if the key wasn't found
     */
    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(resourceBundle.getString(key), params);
        } catch (Exception e) {
            logger.warning("Missing resource key: " + key + " for locale: " + currentLocale);
            return "Missing translation for key: " + key;
        }
    }
}
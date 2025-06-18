package core;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping user types between different languages and internal representations.
 * Provides methods to normalize user types regardless of the current language setting.
 */
public class UserTypeMapper {

    // Internal canonical user type constants
    public static final String STUDENTS = "Students";
    public static final String GENERAL_PUBLIC = "General Public";
    public static final String ADMINS = "Admins";

    /**
     * Maps a localized user type string to its canonical internal representation.
     * This allows the system to work with consistent internal values regardless of language.
     *
     * @param localizedUserType The user type string as it appears in the UI
     * @return The canonical internal representation of the user type
     */
    public static String mapToCanonical(String localizedUserType) {
        if (localizedUserType == null) {
            return null;
        }

        // Create reverse mapping from localized strings to canonical values
        Map<String, String> mappings = new HashMap<>();

        // English mappings
        mappings.put(ResourceManager.getString("user.type.student"), STUDENTS);
        mappings.put(ResourceManager.getString("user.type.public"), GENERAL_PUBLIC);
        mappings.put(ResourceManager.getString("user.type.admin"), ADMINS);

        // Direct canonical mappings (in case already canonical)
        mappings.put(STUDENTS, STUDENTS);
        mappings.put(GENERAL_PUBLIC, GENERAL_PUBLIC);
        mappings.put(ADMINS, ADMINS);

        return mappings.getOrDefault(localizedUserType, localizedUserType);
    }

    /**
     * Maps a canonical user type to its localized representation.
     *
     * @param canonicalUserType The internal canonical user type
     * @return The localized string for display in the UI
     */
    public static String mapToLocalized(String canonicalUserType) {
        if (canonicalUserType == null) {
            return null;
        }

        return switch (canonicalUserType) {
            case STUDENTS -> ResourceManager.getString("user.type.student");
            case GENERAL_PUBLIC -> ResourceManager.getString("user.type.public");
            case ADMINS -> ResourceManager.getString("user.type.admin"); // Add this to your resource files
            default -> canonicalUserType;
        };
    }

    /**
     * Gets all available user types in their localized form.
     *
     * @return Array of localized user type strings
     */
    public static String[] getLocalizedUserTypes() {
        return new String[] {
                ResourceManager.getString("user.type.student"),
                ResourceManager.getString("user.type.public"),
                ResourceManager.getString("user.type.admin")
        };
    }
}

package core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * core.FeeManager class handles the calculation of overdue fees for library books.
 * It provides methods to calculate fees based on user type and days overdue.
 */
public class FeeManager {
    private static final Logger logger = Logger.getLogger(FeeManager.class.getName());

    /** Predefined rates for different user types */
    private static final float STUDENT_RATE = 0.5f;
    private static final float PUBLIC_RATE = 1.0f;
    private static final float ADMIN_RATE = 0.0f;

    /**
     * Calculates the overdue fee based on the number of days overdue and user type.
     *
     * @param daysOverdue The number of days the book is overdue
     * @param userType The type of user (e.g., "Students", "General Public", "Admins")
     * @return The calculated fee, or 0.0 if an error occurs
     */
    public static float calculateFee(int daysOverdue, String userType) {
        try {
            float rate = switch (userType) {
                case "Students" -> STUDENT_RATE;
                case "General Public" -> PUBLIC_RATE;
                case "Admins" -> ADMIN_RATE;
                default -> throw new IllegalArgumentException("Unknown user type: " + userType);
            };

            float fee = daysOverdue * rate;
            logger.log(Level.FINE, "Calculated fee for user type: " + userType + ", days overdue: " + daysOverdue + ", fee: " + fee);

            return fee;

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error calculating fee for user type: " + userType + ", days overdue: " + daysOverdue, e);
            return 0.0f; // Default to no fee in case of error
        }
    }

    /**
     * Calculates the total fines for all overdue books of the current user.
     *
     * @return The total fine amount as a double
     */
    public static double calculateTotalFines() {
        double totalFines = 0.0;

        try {
            if (SessionManager.getInstance().getCurrentUser() == null || SessionManager.getInstance().getKey() == null) {
                return 0.0;
            }

            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBorrowedBooks(SessionManager.getInstance().getCurrentUser(), SessionManager.getInstance().getKey());

            if (books == null || books.isEmpty()) {
                return 0.0;
            }

            String userType = dbm.getUserType(SessionManager.getInstance().getCurrentUser(), SessionManager.getInstance().getKey());
            if (userType == null) {
                return 0.0;
            }

            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();

                    if (!book.has("DateIssued") || !book.has("Status")) {
                        continue;
                    }

                    int status = book.get("Status").getAsInt();
                    if (status == -1) { // Overdue
                        String dateIssued = SecurityManager.decrypt(book.get("DateIssued").getAsString(), SessionManager.getInstance().getKey());
                        String dateDue = dbm.getDueDate(dateIssued, userType);

                        if (dateDue != null) {
                            int daysOverdue = dbm.getDaysOverdue(dateDue);
                            if (daysOverdue > 0) {
                                double fine = calculateFee(daysOverdue, userType);
                                totalFines += fine;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error calculating fine for book at index " + i, e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating total fines", e);
        }

        return totalFines;
    }

}

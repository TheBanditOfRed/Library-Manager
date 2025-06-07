package main.ui.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for calculating overdue fees based on user type and days overdue.
 * This class provides a method to calculate the fee based on predefined rates for different user types.
 */
public class FeeCalculator {
    private static final Logger logger = Logger.getLogger(FeeCalculator.class.getName());

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
}

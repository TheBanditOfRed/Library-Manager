package ui.utils;

import core.SessionManager;
import ui.GUI;
import ui.panels.LoginPanel;
import ui.panels.MainApplicationPanel;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LanguageManager is responsible for managing the application's language settings.
 * It allows users to change the language and updates the UI text accordingly.
 * The class includes error handling for preferences and resource operations.
 */
public class LanguageManager {
    private static final Logger logger = Logger.getLogger(LanguageManager.class.getName());

    /**
     * Changes the application's language and updates the UI text.
     * Includes error handling for preferences and resource operations.
     *
     * @param gui The GUI instance to update
     * @param languageCode The language code to switch to (e.g., "en" for English)
     */
    public static void changeLanguage(GUI gui, String languageCode) {
        String currentLanguage = gui.prefs.get("language", "en");
        logger.log(Level.INFO, "User " + (SessionManager.getInstance().getCurrentUser() != null ? core.SessionManager.getInstance().getCurrentUser() : "unknown") + " changing language from " + currentLanguage + " to " + languageCode);

        try {
            if (languageCode == null || (!languageCode.equals("en") && !languageCode.equals("pt"))) {
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.language.invalid"),
                        core.ResourceManager.getString("error")
                );
                return;
            }

            try {
                gui.prefs.put("language", languageCode);
                logger.log(Level.INFO, "Language preference saved successfully: " + languageCode);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to save language preference", e);
            }

            try {
                core.ResourceManager.setLocale(languageCode);
            } catch (Exception e) {
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.language.resource") + ": " + e.getMessage(),
                        core.ResourceManager.getString("error")
                );
                return;
            }

            JOptionPane.showMessageDialog(gui,
                    core.ResourceManager.getString("options.language.changed"),
                    core.ResourceManager.getString("options.title"),
                    JOptionPane.INFORMATION_MESSAGE);

            try {
                updateUIText(gui);
            } catch (Exception e) {
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.ui.update") + ": " + e.getMessage(),
                        core.ResourceManager.getString("error")
                );
            }
        } catch (Exception e) {
            DialogUtils.showErrorDialog(gui,
                    core.ResourceManager.getString("error.language.general") + ": " + e.getMessage(),
                    core.ResourceManager.getString("error")
            );
        }
    }

    /**
     * Updates all UI text elements when the language changes.
     * Includes error handling for UI component operations.
     * @param gui The GUI instance to update the UI text for
     */
    public static void updateUIText(GUI gui) {
        try {
            gui.setTitle(core.ResourceManager.getString("app.title"));

            String currentCard = StatusUtils.getCurrentCardName(gui.cardLayout);

            try {
                if ("login".equals(currentCard)) {
                    LoginPanel.updateLoginPanel(gui);
                } else if ("main".equals(currentCard)) {
                    MainApplicationPanel.updateMainPanel(gui);
                }
                logger.log(Level.INFO, "UI text updated successfully for new language");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to update UI panels - ", e);
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.ui.update") + ": " + e.getMessage(),
                        core.ResourceManager.getString("error")
                );
            }

            gui.revalidate();
            gui.repaint();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error updating UI text", e);
            DialogUtils.showErrorDialog(gui,
                    core.ResourceManager.getString("error.ui.critical") + ": " + e.getMessage(),
                    core.ResourceManager.getString("error")
            );
        }
    }
}

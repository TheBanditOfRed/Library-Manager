package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Manages the logging system for the library management application.
 * Initializes file and console logging, handles log rotation,
 * and provides custom formatting for log messages.
 */
public class LoggingManager {
    /** Directory and file settings for logging */
    private static final String LOG_DIR = AppDataManager.getLogsDirectory();
    private static final String LOG_FILE_PREFIX = "library-manager_";
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024;
    private static final int MAX_LOG_FILES = 5;
    private static boolean isInitialized = false;
    
    /**
     * Initializes the logging system.
     * Sets up file logging with rotation and console logging with custom formatters.
     * Ensures that the log directory exists and handles any initialization errors gracefully.
     */
    public static synchronized void initializeLogging() {
        if (isInitialized) {
            return; // Already initialized
        }
        
        try {
            createLogDirectory();
            Logger rootLogger = Logger.getLogger("");

            // Remove existing console handlers to avoid duplicates
            Handler[] existingHandlers = rootLogger.getHandlers();
            for (Handler handler : existingHandlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            FileHandler fileHandler = createFileHandler();
            fileHandler.setFormatter(new CustomLogFormatter());
            fileHandler.setLevel(Level.ALL); // File gets everything
            rootLogger.addHandler(fileHandler);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING); // Only warnings/errors to console
            consoleHandler.setFormatter(new SimpleConsoleFormatter());
            rootLogger.addHandler(consoleHandler);

            rootLogger.setLevel(Level.INFO);

            isInitialized = true;

            Logger.getLogger(LoggingManager.class.getName())
                  .info("Logging system initialized. Files: " + Paths.get(LOG_DIR).toAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to initialize file logging: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }

            // Log the stack trace to stderr in a more controlled manner
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.err.println("Stack trace:");
            for (int i = 0; i < Math.min(stackTrace.length, 10); i++) { // Limit to first 10 frames
                System.err.println("  at " + stackTrace[i].toString());
            }
            if (stackTrace.length > 10) {
                System.err.println("  ... " + (stackTrace.length - 10) + " more frames");
            }

            setupFallbackLogging();

        }
    }
    
    /**
     * Creates the log directory if it does not exist.
     * Throws an IOException if the directory cannot be created.
     */
    private static void createLogDirectory() throws IOException {
        Path logPath = Paths.get(LOG_DIR);
        if (!Files.exists(logPath)) {
            Files.createDirectories(logPath);
            System.out.println("Created log directory: " + logPath.toAbsolutePath());
        }
    }

    /**
     * Creates a FileHandler for logging with rotation.
     * The log file pattern includes a timestamp and supports multiple log files.
     *
     * @return A configured FileHandler instance
     * @throws IOException if the file handler cannot be created
     */
    private static FileHandler createFileHandler() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFilePattern = LOG_DIR + "/" + LOG_FILE_PREFIX + timestamp + "_%g.log";

        return new FileHandler(
            logFilePattern,
            MAX_LOG_SIZE,
            MAX_LOG_FILES,
            true
        );
    }
    
    /**
     * Sets up fallback logging to console if file logging fails.
     * This ensures that critical errors can still be logged to the console.
     */
    private static void setupFallbackLogging() {
        try {
            Logger rootLogger = Logger.getLogger("");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(consoleHandler);
            rootLogger.setLevel(Level.INFO);
            
            Logger.getLogger(LoggingManager.class.getName())
                  .warning("Using fallback console logging due to file logging failure");
        } catch (Exception fallbackException) {
            System.err.println("CRITICAL: Even fallback logging failed!");
        }
    }
    
    /**
     * Custom log formatter that formats log messages with timestamp, level, thread name,
     * logger name, and exception details if present.
     */
    private static class CustomLogFormatter extends Formatter {
        private final DateTimeFormatter dateFormatter = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            // Log name and info
            sb.append(LocalDateTime.now().format(dateFormatter));
            sb.append(" ");

            sb.append(String.format("%-7s", record.getLevel()));
            sb.append(" ");

            sb.append(String.format("[%-15s]", Thread.currentThread().getName()));
            sb.append(" ");

            String loggerName = record.getLoggerName();
            if (loggerName != null && loggerName.contains(".")) {
                loggerName = loggerName.substring(loggerName.lastIndexOf(".") + 1);
            }
            sb.append(String.format("%-20s", loggerName != null ? loggerName : "Unknown"));
            sb.append(" - ");

            // Message
            sb.append(formatMessage(record));
            sb.append(System.lineSeparator());
            
            // Exception details
            if (record.getThrown() != null) {
                sb.append("Exception: ");
                sb.append(record.getThrown().getClass().getSimpleName());
                sb.append(": ");
                sb.append(record.getThrown().getMessage());
                sb.append(System.lineSeparator());
                
                // Stack trace (first few lines only to avoid huge logs)
                StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
                int linesToShow = Math.min(5, stackTrace.length);
                for (int i = 0; i < linesToShow; i++) {
                    sb.append("    at ");
                    sb.append(stackTrace[i].toString());
                    sb.append(System.lineSeparator());
                }
                if (stackTrace.length > linesToShow) {
                    sb.append("    ... ").append(stackTrace.length - linesToShow).append(" more lines");
                    sb.append(System.lineSeparator());
                }
            }
            
            return sb.toString();
        }
    }

    /**
     * Simple console formatter that formats log messages for console output.
     * It includes the log level, logger name, and message.
     */
    private static class SimpleConsoleFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s %s: %s%s",
                record.getLevel(),
                record.getLoggerName() != null ? 
                    record.getLoggerName().substring(record.getLoggerName().lastIndexOf(".") + 1) : 
                    "Unknown",
                formatMessage(record),
                System.lineSeparator()
            );
        }
    }
    
    /**
     * Gets the current log directory path.
     * 
     * @return The absolute path to the log directory as a string
     */
    public static String getLogDirectory() {
        return Paths.get(LOG_DIR).toAbsolutePath().toString();
    }
    
    /**
     * Manually flushes all log handlers to ensure pending log messages are written to their destinations.
     * This method iterates through all handlers attached to the root logger and calls flush() on each one.
     * Useful for ensuring logs are written before application shutdown or at critical points.
     */
    public static void flushLogs() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.flush();
        }
    }
}
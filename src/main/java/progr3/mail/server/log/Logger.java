package progr3.mail.server.log;

import progr3.mail.server.io.IJsonFileHandler;

public class Logger implements ILogger {
    private LogLevel currentLogLevel;
    private String logFilePath;
    private boolean logToFile;
    private boolean logToConsole;
    private IJsonFileHandler jsonFileHandler;

    public Logger(LogLevel level, String logFilePath, boolean logToFile, boolean logToConsole,
            IJsonFileHandler jsonFileHandler) {
        this.currentLogLevel = level;
        this.logFilePath = logFilePath;
        this.logToFile = logToFile;
        this.logToConsole = logToConsole;
        this.jsonFileHandler = jsonFileHandler;
    }

    private void writeLog(String message) {
        if (logToConsole) {
            System.out.println(message);
        }
        if (logToFile) {
            try {
                jsonFileHandler.saveToFile(message, logFilePath, String.class);
            } catch (Exception e) {
                System.err.println("Failed to write log to file: " + e.getMessage());
            }
        }
    }

    @Override
    public void logInfo(String message) {
        if (currentLogLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            writeLog("[INFO] " + message);
        }
    }

    @Override
    public void logError(String message, Throwable throwable) {
        if (currentLogLevel.ordinal() >= LogLevel.ERROR.ordinal()) {
            writeLog("[ERROR] " + message);
        }
    }

    @Override
    public void logDebug(String message) {
        if (currentLogLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            writeLog("[DEBUG] " + message);
        }
    }

}

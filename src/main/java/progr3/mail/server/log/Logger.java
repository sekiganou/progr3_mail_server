package progr3.mail.server.log;

import java.io.File;

import progr3.mail.server.io.IJsonFileHandler;
import progr3.mail.server.model.Log;
import progr3.mail.server.model.Log.LogLevel;

public class Logger implements ILogger {
    private LogLevelEnum currentLogLevel;
    private String logFilePath;
    private boolean logToFile;
    private boolean logToConsole;
    private IJsonFileHandler jsonFileHandler;

    private boolean isInScope = false;

    public Logger(
            LogLevelEnum level, String logFilePath, boolean logToFile, boolean logToConsole,
            IJsonFileHandler jsonFileHandler) {
        this.currentLogLevel = level;
        this.logFilePath = logFilePath;
        this.logToFile = logToFile;
        this.logToConsole = logToConsole;
        this.jsonFileHandler = jsonFileHandler;

        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    public void startScope() {
        if (isInScope) {
            throw new IllegalStateException("Logging scope already started.");
        }

        LogContext.generateAndSetRequestId();
        isInScope = true;
    }

    public void endScope() {
        if (!isInScope) {
            throw new IllegalStateException("No active logging scope to end.");
        }

        LogContext.clear();
        isInScope = false;
    }

    private void writeLog(LogLevel logLevel, String message) {
        var requestId = LogContext.getAndIncrementRequestId();
        Log log = LogConstructor.create(requestId, logLevel, message);
        String logMessage = "[" + requestId + "] " + "[" + logLevel + "] " + message;
        if (logToConsole) {
            System.out.println(logMessage);
        }
        if (logToFile) {
            try {
                jsonFileHandler.saveToFile(log, logFilePath, Log.class);
            } catch (Exception e) {
                System.err.println("Failed to write log to file: " + e.getMessage());
            }
        }
    }

    @Override
    public void logInfo(String message) {
        if (currentLogLevel.ordinal() >= LogLevelEnum.INFO.ordinal()) {
            writeLog(LogLevel.INFO, message);
        }
    }

    @Override
    public void logError(String message, Throwable throwable) {
        if (currentLogLevel.ordinal() >= LogLevelEnum.ERROR.ordinal()) {
            writeLog(LogLevel.ERROR, message);
        }
    }

    @Override
    public void logDebug(String message) {
        if (currentLogLevel.ordinal() >= LogLevelEnum.DEBUG.ordinal()) {
            writeLog(LogLevel.DEBUG, message);
        }
    }

}

package progr3.mail.server.log;

public class Logger implements ILogger {
    private LogLevel currentLogLevel = LogLevel.INFO;

    @Override
    public void logInfo(String message) {
        if (currentLogLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            System.out.println("[INFO] " + message);
        }
    }

    @Override
    public void logError(String message, Throwable throwable) {
        if (currentLogLevel.ordinal() >= LogLevel.ERROR.ordinal()) {
            System.err.println("[ERROR] " + message);
            throwable.printStackTrace(System.err);
        }
    }

    @Override
    public void logDebug(String message) {
        if (currentLogLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            System.out.println("[DEBUG] " + message);
        }
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.currentLogLevel = level;
    }

}

package progr3.mail.server.app;

public interface ILogger {
    void logInfo(String message);

    void logError(String message, Throwable throwable);

    void logDebug(String message);

    void setLogLevel(LogLevel level);
}

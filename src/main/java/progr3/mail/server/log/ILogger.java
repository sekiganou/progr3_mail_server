package progr3.mail.server.log;

public interface ILogger {
    void startScope();

    void endScope();

    void logInfo(String message);

    void logError(String message, Throwable throwable);

    void logDebug(String message);

}

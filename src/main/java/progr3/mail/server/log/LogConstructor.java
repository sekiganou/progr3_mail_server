package progr3.mail.server.log;

import java.util.Date;

import progr3.mail.server.model.Log;
import progr3.mail.server.model.Log.LogLevel;

public class LogConstructor {
    protected static Log create(String requestId, LogLevel level, String message, String details) {
        Log log = new Log();
        log.setLogLevel(level);
        log.setMessage(message);
        log.setRequestId(requestId);
        log.setTimestamp(new Date());
        log.setDetails(details);
        return log;
    }
}

package progr3.mail.server.log;

import java.util.concurrent.atomic.AtomicInteger;

public class LogContext {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    protected static void setRequestId(String requestId) {
        requestIdHolder.set(requestId);
    }

    protected static String getRequestId() {
        return requestIdHolder.get();
    }

    protected static String generateAndSetRequestId() {
        String requestId = "REQ-" + counter.incrementAndGet();
        requestIdHolder.set(requestId);
        return requestId;
    }

    protected static void clear() {
        requestIdHolder.remove();
    }
}

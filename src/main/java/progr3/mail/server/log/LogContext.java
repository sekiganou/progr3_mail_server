package progr3.mail.server.log;

import java.security.SecureRandom;

public class LogContext {
    private static final int INITAL_COUNTER_VALUE = 1;
    private static final int REQUEST_ID_LENGTH = 8;
    private static final ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> INITAL_COUNTER_VALUE);
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    private static String getAlphaNumericString(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    protected static String getRequestId() {
        return requestIdHolder.get();
    }

    protected static String getAndIncrementRequestId() {
        var requestId = requestIdHolder.get() + "-" + counter.get();
        counter.set(counter.get() + 1);
        return requestId;
    }

    protected static String generateAndSetRequestId() {
        String requestId = "REQ-" + getAlphaNumericString(REQUEST_ID_LENGTH);
        requestIdHolder.set(requestId);
        return requestId;
    }

    protected static void clear() {
        counter.set(INITAL_COUNTER_VALUE);
        requestIdHolder.remove();
    }
}

package progr3.mail.server.log;

import java.nio.charset.Charset;
import java.util.Random;

public class LogContext {
    private static final int INITAL_COUNTER_VALUE = 1;
    private static final int REQUEST_ID_LENGTH = 8;
    private static final ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> INITAL_COUNTER_VALUE);
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    private static String getAlphaNumericString(int n) {

        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        StringBuffer r = new StringBuffer();
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if
            // ((ch >= 'a' && ch <= 'z')
            (ch >= 'A' && ch <= 'Z'
            // || (ch >= '0' && ch <= '9'))
                    && n > 0) {

                r.append(ch);
                n--;
            }
        }

        return r.toString();
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

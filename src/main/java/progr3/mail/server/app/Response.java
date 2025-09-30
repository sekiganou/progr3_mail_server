package progr3.mail.server.app;

public class Response {

    public boolean success;
    public String message;
    public Object data;

    private Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    private Response(boolean success, String message) {
        this(success, message, null);
    }

    private Response(boolean success) {
        this(success, null, null);
    }

    public static Response success(Object data) {
        return new Response(true, null, data);
    }

    public static Response success() {
        return new Response(true);
    }

    public static Response failure(String message) {
        return new Response(false, message);
    }
}

package progr3.mail.server.app;

public class Request {

    public RequestType type;
    public String userId;

    public Request(RequestType type, String userId) {
        this.type = type;
        this.userId = userId;
    }
}

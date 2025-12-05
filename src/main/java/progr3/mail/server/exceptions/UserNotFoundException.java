package progr3.mail.server.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}

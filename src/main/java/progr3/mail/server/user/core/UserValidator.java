package progr3.mail.server.user.core;

import progr3.mail.server.model.User;

public class UserValidator {
    public static String isValidUser(User user) {
        if (user == null) {
            return "User cannot be null";
        }
        if (user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            return "Invalid email";
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            return "Invalid name";
        }
        return null; // User is valid
    }

}

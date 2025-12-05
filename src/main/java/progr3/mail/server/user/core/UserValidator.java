package progr3.mail.server.user.core;

import progr3.mail.server.model.User;

public class UserValidator {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && !email.isEmpty() && email.matches(emailRegex);
    }

    public static String isValidUser(User user) {
        if (user == null) {
            return "User cannot be null";
        }
        if (!isValidEmail(user.getEmail())) {
            return "Invalid email";
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            return "Invalid name";
        }
        return null; // User is valid
    }

}

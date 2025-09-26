package progr3.mail.server.message;

import java.util.List;

public class MessageValidator {
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty();
    }

    public static boolean isValidSubject(String subject) {
        return subject != null && !subject.trim().isEmpty() && subject.length() <= 255;
    }

    public static boolean isValidBody(String body) {
        return body != null && !body.trim().isEmpty() && body.length() <= 1000;
    }

    public static boolean isValidUserId(String userId) {
        return userId != null && !userId.trim().isEmpty();
    }

    public static boolean isValidMessageId(String messageId) {
        return messageId != null && !messageId.trim().isEmpty();
    }

    public static boolean areValidRecipients(List<String> recipientsUserEmails) {
        if (recipientsUserEmails == null || recipientsUserEmails.isEmpty()) {
            return false;
        }
        return true;
    }

}

package progr3.mail.server.message.core;

import progr3.mail.server.model.Message;

public class MessageValidator {
    public static String isValidMessage(Message message) {
        if (message == null) {
            return "Message cannot be null";
        }
        if (message.getSenderUserGUID() == null || message.getSenderUserGUID().isEmpty()) {
            return "Invalid sender user ID";
        }
        if (message.getRecipientsUserEmails() == null || message.getRecipientsUserEmails().isEmpty()) {
            return "Invalid recipients list";
        }
        for (String recipient : message.getRecipientsUserEmails()) {
            if (recipient == null || recipient.isEmpty()) {
                return "Invalid recipient email in recipients list";
            }
        }
        if (message.getSubject() == null) {
            return "Invalid subject";
        }
        if (message.getBody() == null) {
            return "Invalid body";
        }
        if (message.getIsForwarded() == null) {
            return "Invalid isForwarded flag";
        }
        return null; // Message is valid
    }

}

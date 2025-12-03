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
        if (message.getRecipientsUserGUIDs() == null || message.getRecipientsUserGUIDs().isEmpty()) {
            return "Invalid recipients list";
        }
        for (String recipient : message.getRecipientsUserGUIDs()) {
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
        return null; // Message is valid
    }

}

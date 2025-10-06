package progr3.mail.server.message.core;

import java.util.List;
import java.util.UUID;

import progr3.mail.server.model.Message;
import progr3.mail.server.model.Message.IsForwarded;

public class MessageConstructor {
    public static Message create(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {
        var message = new Message();
        message.setSenderUserGUID(senderUserId);
        message.setRecipientsUserEmails(recipientsUserEmails);
        message.setSubject(subject);
        message.setBody(body);
        message.setGuid(UUID.randomUUID().toString());
        message.setDate(new java.util.Date());
        message.setIsForwarded(IsForwarded.NO);
        return message;
    }

    public static Message copyFrom(Message message) {
        var newMessage = new Message();
        newMessage.setSenderUserGUID(message.getSenderUserGUID());
        newMessage.setRecipientsUserEmails(message.getRecipientsUserEmails());
        newMessage.setSubject(message.getSubject());
        newMessage.setBody(message.getBody());
        newMessage.setGuid(UUID.randomUUID().toString());
        newMessage.setDate(new java.util.Date());
        newMessage.setIsForwarded(message.getIsForwarded());
        return newMessage;
    }

}

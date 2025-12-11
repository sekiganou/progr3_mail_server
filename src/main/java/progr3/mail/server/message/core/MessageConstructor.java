package progr3.mail.server.message.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import progr3.mail.server.model.Message;

public class MessageConstructor {
    public static Message create(String senderUserId, List<String> recipientsUserGUIDs, String subject, String body) {
        var message = new Message();
        message.setSenderUserGUID(senderUserId);
        message.setRecipientsUserGUIDs(recipientsUserGUIDs);
        message.setDeletedRecipientsUserGUIDs(new ArrayList<String>());
        message.setSubject(subject);
        message.setBody(body);
        message.setGuid(UUID.randomUUID().toString());
        message.setDate(new Date());
        return message;
    }

    public static Message clone(Message message) {
        var newMessage = new Message();
        newMessage.setSenderUserGUID(message.getSenderUserGUID());
        newMessage.setRecipientsUserGUIDs(message.getRecipientsUserGUIDs());
        newMessage.setDeletedRecipientsUserGUIDs(new ArrayList<String>(message.getDeletedRecipientsUserGUIDs()));
        newMessage.setSubject(message.getSubject());
        newMessage.setBody(message.getBody());
        newMessage.setGuid(UUID.randomUUID().toString());
        newMessage.setDate(message.getDate());
        return newMessage;
    }

}

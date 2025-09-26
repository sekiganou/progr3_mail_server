package progr3.mail.server.message;

import java.util.List;

import progr3.mail.server.model.Message;

public interface IMessageRepository {
        List<Message> getAllUserMessages(String userId);

        Message getMessageDetails(String messageId);

        boolean saveMessage(Message message);

        boolean deleteMessage(String messageId);
}

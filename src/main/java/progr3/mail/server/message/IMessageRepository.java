package progr3.mail.server.message;

import java.io.IOException;
import java.util.List;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.model.Message;

public interface IMessageRepository {
        List<Message> getAllUserMessages(String userId) throws UserNotFoundException, BadRequestException;

        Message getMessageDetails(String messageId) throws MessageNotFoundException, BadRequestException;

        String saveMessage(Message message) throws BadRequestException, IOException;

        void deleteMessage(String messageId) throws BadRequestException, MessageNotFoundException, IOException;
}

package progr3.mail.server.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.core.MessageConstructor;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.Message.IsForwarded;
import progr3.mail.server.user.IUserRepository;

public class MessageService {

    private ILogger logger;
    private IMessageRepository messageRepository;
    private IUserRepository userRepository;

    public MessageService(IMessageRepository messageRepository,
            IUserRepository userRepository, ILogger logger) {
        this.logger = logger;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public String sendMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body)
            throws BadRequestException, IOException, UserNotFoundException {
        logger.logInfo(
                "Sending message from user: " + senderUserId + " to recipients: "
                        + recipientsUserEmails);

        var message = MessageConstructor.create(senderUserId,
                recipientsUserEmails,
                subject,
                body);

        return messageRepository.saveMessage(message);
    }

    public String replySingleToMessage(String senderUserId, String messageId, String subject,
            String body) throws MessageNotFoundException, BadRequestException, IOException {
        logger.logInfo("Replying to message: " + messageId + " from user: " + senderUserId);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        var messageReply = MessageConstructor.create(
                senderUserId,
                List.of(originalMessage.getSenderUserGUID()),
                subject,
                body);

        return messageRepository.saveMessage(messageReply);
    };

    public String replyAllToMessage(String senderUserId, String messageId,
            String subject, String body) throws MessageNotFoundException, BadRequestException, IOException {
        logger.logInfo("Replying all to message: " + messageId + " from user: " + senderUserId);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        var recipients = new ArrayList<>(originalMessage.getRecipientsUserEmails());
        if (!recipients.add(originalMessage.getSenderUserGUID())) {
            return null;
        }

        var messageReply = MessageConstructor.create(
                senderUserId,
                recipients,
                subject,
                body);

        return messageRepository.saveMessage(messageReply);
    }

    public String forwardMessage(String forwarderUserId, String messageId,
            List<String> recipientsUserEmails) throws MessageNotFoundException, BadRequestException, IOException {
        logger.logInfo("Forwarding message: " + messageId + " from user: " + forwarderUserId + " to recipients: "
                + recipientsUserEmails);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        var newMessage = MessageConstructor.copyFrom(originalMessage);
        newMessage.setSenderUserGUID(forwarderUserId);
        newMessage.setRecipientsUserEmails(recipientsUserEmails);
        newMessage.setIsForwarded(IsForwarded.YES);

        return messageRepository.saveMessage(newMessage);

    }

    public List<Message> getAllUserMessages(String userId) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Retrieving all messages for user: " + userId);

        return messageRepository.getAllUserMessages(userId);
    };

    public Message getMessageDetails(String messageId) throws MessageNotFoundException, BadRequestException {
        logger.logInfo("Retrieving message details for message ID: " + messageId);

        return messageRepository.getMessageDetails(messageId);
    }

    public void deleteMessage(String messageId) throws MessageNotFoundException, BadRequestException, IOException {
        logger.logInfo("Deleting message with ID: " + messageId);
        messageRepository.deleteMessage(messageId);
    }

}

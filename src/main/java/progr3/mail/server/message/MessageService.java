package progr3.mail.server.message;

import java.util.ArrayList;
import java.util.List;

import progr3.mail.server.log.ILogger;
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

    public String sendMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {
        logger.logInfo("Sending message from user: " + senderUserId + " to recipients: " + recipientsUserEmails);

        for (String recipientUser : recipientsUserEmails) {
            if (userRepository.getUserByEmail(recipientUser) == null) {
                logger.logError("Recipient user not found: " + recipientUser, null);
                return null;
            }
        }

        var message = MessageConstructor.create(senderUserId,
                recipientsUserEmails,
                subject,
                body);

        return messageRepository.saveMessage(message) ? message.getGuid() : null;
    }

    public String replySingleToMessage(String senderUserId, String messageId, String subject,
            String body) {
        logger.logInfo("Replying to message: " + messageId + " from user: " + senderUserId);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        if (originalMessage == null) {
            logger.logError("Original message not found: " + messageId, null);
            return null;
        }

        var messageReply = MessageConstructor.create(
                senderUserId,
                List.of(originalMessage.getSenderUserGUID()),
                subject,
                body);

        return messageRepository.saveMessage(messageReply) ? messageReply.getGuid() : null;
    };

    public String replyAllToMessage(String senderUserId, String messageId,
            String subject, String body) {
        logger.logInfo("Replying all to message: " + messageId + " from user: " + senderUserId);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        if (originalMessage == null) {
            logger.logError("Original message not found: " + messageId, null);
            return null;
        }

        var recipients = new ArrayList<>(originalMessage.getRecipientsUserGUIDs());
        if (!recipients.add(originalMessage.getSenderUserGUID()))
            return null;

        var messageReply = MessageConstructor.create(
                senderUserId,
                recipients,
                subject,
                body);

        return messageRepository.saveMessage(messageReply) ? messageReply.getGuid() : null;
    }

    public String forwardMessage(String forwarderUserId, String messageId,
            List<String> recipientsUserEmails) {
        logger.logInfo("Forwarding message: " + messageId + " from user: " + forwarderUserId + " to recipients: "
                + recipientsUserEmails);

        var originalMessage = messageRepository.getMessageDetails(messageId);

        if (originalMessage == null) {
            logger.logError("Original message not found: " + messageId, null);
            return null;
        }
        var newMessage = MessageConstructor.copyFrom(originalMessage);
        newMessage.setSenderUserGUID(forwarderUserId);
        newMessage.setRecipientsUserGUIDs(recipientsUserEmails);
        newMessage.setIsForwarded(IsForwarded.YES);

        return messageRepository.saveMessage(newMessage) ? newMessage.getGuid() : null;

    }

    public List<Message> getAllUserMessages(String userId) {
        logger.logInfo("Retrieving all messages for user: " + userId);

        if (userId == null || userId.isEmpty()) {
            logger.logError("User ID is null", null);
            return new ArrayList<>();
        }

        if (userRepository.getUserById(userId) == null) {
            logger.logError("User not found: " + userId, null);
            return new ArrayList<>();
        }

        return messageRepository.getAllUserMessages(userId);
    };

    public Message getMessageDetails(String messageId) {
        logger.logInfo("Retrieving message details for message ID: " + messageId);

        if (messageId == null || messageId.isEmpty()) {
            logger.logError("Message ID is null", null);
            return null;
        }

        if (messageRepository.getMessageDetails(messageId) == null) {
            logger.logError("Message not found: " + messageId, null);
            return null;
        }

        return messageRepository.getMessageDetails(messageId);
    }

    public boolean deleteMessage(String messageId) {
        logger.logInfo("Deleting message with ID: " + messageId);

        if (messageId == null || messageId.isEmpty()) {
            logger.logError("Message ID is null", null);
            return false;
        }

        if (messageRepository.getMessageDetails(messageId) == null) {
            logger.logError("Message not found: " + messageId, null);
            return false;
        }

        return messageRepository.deleteMessage(messageId);
    }

}

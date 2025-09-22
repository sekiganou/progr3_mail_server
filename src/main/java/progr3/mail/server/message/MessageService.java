package progr3.mail.server.message;

import java.util.List;
import java.util.UUID;

import progr3.mail.server.app.ILogger;
import progr3.mail.server.model.Message;
import progr3.mail.server.user.IUserRepository;

public class MessageService {

    private ILogger logger;
    private IMessageRepository messageRepository;
    private IUserRepository userRepository;

    public MessageService(ILogger logger,
            IMessageRepository messageRepository,
            IUserRepository userRepository) {
        this.logger = logger;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    private Message createMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {
        var message = new Message();
        message.setSenderUserGUID(senderUserId);
        message.setRecipientsUserGUIDs(recipientsUserEmails);
        message.setSubject(subject);
        message.setBody(body);
        message.setGuid(UUID.randomUUID().toString());
        message.setDate(new java.util.Date());
        return message;
    }

    public String sendMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {

        for (String recipientUser : recipientsUserEmails) {
            if (userRepository.getUserDetailsByEmail(recipientUser) == null) {
                logger.logError("Recipient user not found: " + recipientUser, null);
                return null;
            }
        }

        var message = createMessage(senderUserId,
                recipientsUserEmails,
                subject,
                body);

        try {
            messageRepository.saveMessage(message);
            return message.getGuid();
        } catch (Exception e) {
            logger.logError("Failed to save message", e);
            return null;
        }
    }

    public String replySingleToMessage(String senderUserId, String messageId, String subject,
            String body) {
        var originalMessage = messageRepository.getMessageDetails(messageId);
        var messageReply = createMessage(
                senderUserId,
                List.of(originalMessage.getSenderUserGUID()),
                subject,
                body);

        try {
            messageRepository.saveMessage(messageReply);
            return messageReply.getGuid();
        } catch (Exception e) {
            logger.logError("Failed to save message", e);
            return null;
        }

    };

    public String replyAllToMessage(String messageId,
            String subject, String body) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replyAllToMessage'");
    }

    public boolean forwardMessage(String forwarderUserId, List<String> recipientsUserEmails) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forwardMessage'");
    }

    public List<Message> getAllUserMessages(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUserMessages'");
    };

    public Message getMessageDetails(String messageId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMessageDetails'");
    }

    public boolean deleteMessage(String messageId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteMessage'");
    }

}

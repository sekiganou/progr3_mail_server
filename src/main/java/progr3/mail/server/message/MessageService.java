package progr3.mail.server.message;

import java.util.List;

import progr3.mail.server.app.ILogger;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.Message.IsForwarded;
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

    private class Validator {
        public static boolean isValidEmail(String email) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            return email != null && email.matches(emailRegex);
        }

        public static boolean isValidSubject(String subject) {
            return subject != null && !subject.trim().isEmpty() && subject.length() <= 255;
        }

        public static boolean isValidBody(String body) {
            return body != null && !body.trim().isEmpty();
        }

        public static boolean isValidUserId(String userId) {
            return userId != null && !userId.trim().isEmpty();
        }

        public static boolean isValidMessageId(String messageId) {
            return messageId != null && !messageId.trim().isEmpty();
        }

        public static boolean areValidRecipients(List<String> recipients) {
            if (recipients == null || recipients.isEmpty()) {
                return false;
            }
            for (String email : recipients) {
                if (!isValidEmail(email)) {
                    return false;
                }
            }
            return true;
        }
    }

    public String sendMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {

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
        var originalMessage = messageRepository.getMessageDetails(messageId);
        var messageReply = MessageConstructor.create(
                senderUserId,
                List.of(originalMessage.getSenderUserGUID()),
                subject,
                body);

        return messageRepository.saveMessage(messageReply) ? messageReply.getGuid() : null;
    };

    public String replyAllToMessage(String senderUserId, String messageId,
            String subject, String body) {
        var originalMessage = messageRepository.getMessageDetails(messageId);
        var recipients = originalMessage.getRecipientsUserGUIDs();
        recipients.add(originalMessage.getSenderUserGUID());
        var messageReply = MessageConstructor.create(
                senderUserId,
                recipients,
                subject,
                body);

        ;
        return messageRepository.saveMessage(messageReply) ? messageReply.getGuid() : null;
    }

    public boolean forwardMessage(String forwarderUserId, String messageId,
            List<String> recipientsUserEmails) {

        var originalMessage = messageRepository.getMessageDetails(messageId);
        originalMessage.setIsForwarded(IsForwarded.YES);
        originalMessage.setSenderUserGUID(forwarderUserId);
        originalMessage.setRecipientsUserGUIDs(recipientsUserEmails);

        return messageRepository.saveMessage(originalMessage);

    }

    public List<Message> getAllUserMessages(String userId) {
        if (userId == null || userId.isEmpty()) {
            logger.logError("User ID is null", null);
            return List.of();
        }

        if (userRepository.getUserById(userId) == null) {
            logger.logError("User not found: " + userId, null);
            return List.of();
        }

        return messageRepository.getAllMessages(userId);
    };

    public Message getMessageDetails(String messageId) {
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

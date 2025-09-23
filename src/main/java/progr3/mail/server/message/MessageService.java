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

    public String sendMessage(String senderUserId, List<String> recipientsUserEmails, String subject, String body) {

        for (String recipientUser : recipientsUserEmails) {
            if (userRepository.getUserDetailsByEmail(recipientUser) == null) {
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
        return messageRepository.getAllMessages(userId);
    };

    public Message getMessageDetails(String messageId) {
        return messageRepository.getMessageDetails(messageId);
    }

    public boolean deleteMessage(String messageId) {
        return messageRepository.deleteMessage(messageId);
    }

}

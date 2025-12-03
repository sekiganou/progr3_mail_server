package progr3.mail.server.message;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.core.MessageConstructor;
import progr3.mail.server.model.Message;
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

        var recipientsUserGUIDs = new ArrayList<String>();
        for (var email : recipientsUserEmails) {
            var user = userRepository.getUserByEmail(email);
            recipientsUserGUIDs.add(user.getGuid());
        }

        var message = MessageConstructor.create(senderUserId,
                recipientsUserGUIDs,
                subject,
                body);

        return messageRepository.saveMessage(message);
    }

    public List<Message> getAllUserMessages(String userId) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Retrieving all messages for user: " + userId);

        return messageRepository.getAllUserMessages(userId);
    };

    public List<Message> getUserMessagesWithFilters(String userId, Date startDate, Date endDate)
            throws UserNotFoundException, BadRequestException {
        logger.logInfo("Retrieving all messages for user: " + userId + " with date filter from " + startDate + " to "
                + endDate);

        var allMessages = messageRepository.getAllUserMessages(userId);

        if (startDate == null && endDate == null) {
            return allMessages;
        }

        var filteredMessages = new ArrayList<Message>();

        for (var message : allMessages) {
            if (message.getDate().after(startDate) && message.getDate().before(endDate)) {
                filteredMessages.add(message);
            }
        }

        return filteredMessages;
    }

    public Message getMessageDetails(String messageId) throws MessageNotFoundException, BadRequestException {
        logger.logInfo("Retrieving message details for message ID: " + messageId);

        return messageRepository.getMessageDetails(messageId);
    }

    public void deleteMessage(String messageId, String userId)
            throws MessageNotFoundException, BadRequestException, IOException {
        Message message = messageRepository.getMessageDetails(messageId);

        var recipients = new ArrayList<>(message.getRecipientsUserGUIDs());
        if (!recipients.contains(userId)) {
            throw new BadRequestException("User is not a recipient of the message");
        }

        var newMessage = MessageConstructor.copyFrom(message);
        var deletedRecipients = new ArrayList<>(message.getDeletedRecipientsUserGUIDs());

        newMessage.setGuid(messageId);
        deletedRecipients.add(userId);
        newMessage.setDeletedRecipientsUserGUIDs(deletedRecipients);

        if (deletedRecipients.equals(recipients)) {
            // this is completely arbitrary, it's just to save on some space disk
            logger.logInfo(
                    "All recipients have deleted the message with ID: " + messageId + ". Removing message entirely.");
            messageRepository.deleteMessage(messageId);
        } else {
            logger.logInfo(
                    "User with ID: " + userId + " has deleted the message with ID: " + messageId
                            + ". Updating message.");
            messageRepository.updateMessage(newMessage);
        }

    }

}

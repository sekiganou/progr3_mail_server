package progr3.mail.server.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.io.IJsonFileHandler;
import progr3.mail.server.message.core.MessageValidator;
import progr3.mail.server.model.Message;

public class MessageRepository implements IMessageRepository {

    private final IJsonFileHandler jsonFileHandler;
    private final Map<String, List<Message>> messagesByUserId = new HashMap<>();
    private final Map<String, Message> messagesById = new HashMap<>();
    private final String filePath;

    public MessageRepository(IJsonFileHandler jsonFileHandler, String filePath) {
        this.jsonFileHandler = jsonFileHandler;
        this.filePath = filePath;
        this.loadMessagesFromFile();
    }

    private void loadMessagesFromFile() {
        List<Message> messages = new ArrayList<>();
        try {
            messages = jsonFileHandler.loadFromFile(filePath, Message.class);
            for (Message message : messages) {

                for (String recipientGUID : message.getRecipientsUserGUIDs()) {
                    messagesByUserId.computeIfAbsent(recipientGUID, k -> new ArrayList<>())
                            .add(message);
                }
                messagesById.put(message.getGuid(), message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> getAllUserMessages(String userId) throws BadRequestException {
        if (userId == null || userId.isEmpty())
            throw new BadRequestException("User ID cannot be null");
        return messagesByUserId.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public Message getMessageDetails(String messageId) throws MessageNotFoundException {
        var message = messagesById.get(messageId);
        if (message == null) {
            throw new MessageNotFoundException();
        }
        return message;
    }

    @Override
    public String saveMessage(Message message) throws BadRequestException, IOException {
        var validationError = MessageValidator.isValidMessage(message);

        if (validationError != null) {
            throw new BadRequestException(validationError);
        }

        if (messagesById.get(message.getGuid()) != null) {
            return message.getGuid();
        }

        messagesById.put(message.getGuid(), message);
        for (String recipientGUID : message.getRecipientsUserGUIDs()) {
            messagesByUserId.computeIfAbsent(
                    recipientGUID, k -> new ArrayList<>())
                    .add(message);
        }

        jsonFileHandler.saveToFile(message, filePath, Message.class);
        return message.getGuid();

    }

    @Override
    public void deleteMessage(String messageId) throws MessageNotFoundException, BadRequestException, IOException {
        if (messageId == null)
            throw new BadRequestException("Message ID cannot be null");

        var message = messagesById.get(messageId);

        if (message == null) {
            throw new MessageNotFoundException();
        }

        var validationError = MessageValidator.isValidMessage(message);
        if (validationError != null) {
            throw new BadRequestException(validationError);
        }

        var removedMessage = messagesById.remove(messageId);
        var removedMessagesFromUsers = messagesByUserId.values().stream()
                .map(list -> list.removeIf(msg -> msg.getGuid().equals(messageId)))
                .reduce((a, b) -> a || b)
                .orElse(false);

        if (removedMessage == null || !removedMessagesFromUsers) {
            throw new MessageNotFoundException();
        }

        jsonFileHandler.removeFromFile(message, filePath, Message.class);
    }

}
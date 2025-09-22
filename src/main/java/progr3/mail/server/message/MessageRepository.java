package progr3.mail.server.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.io.IJsonFileHandler;
import progr3.mail.server.model.Message;

public class MessageRepository implements IMessageRepository {

    private final IJsonFileHandler jsonFileHandler;
    private final Map<String, Message> messagesById = new HashMap<>();
    private final String filePath;

    public MessageRepository(IJsonFileHandler jsonFileHandler, String filePath) {
        this.jsonFileHandler = jsonFileHandler;
        this.filePath = filePath;
        this.loadMessagesFromFile();
    }

    private void loadMessagesFromFile() {
        List<Message> messages = List.of();
        try {
            messages = jsonFileHandler.loadFromFile(filePath, Message.class);
            for (Message message : messages) {
                messagesById.put(message.getGuid(), message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean saveMessageToFile(Message message) {
        try {
            jsonFileHandler.saveToFile(message, filePath, Message.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteMessageFromFile(Message message) {
        try {
            jsonFileHandler.removeFromFile(message, filePath, Message.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Message> getAllMessages() {
        return List.copyOf(messagesById.values());
    }

    @Override
    public Message getMessageDetails(String messageId) {
        return messagesById.get(messageId);
    }

    @Override
    public boolean saveMessage(Message message) {
        if (messagesById.get(message.getGuid()) != null) {
            return false;
        }
        messagesById.put(message.getGuid(), message);
        return saveMessageToFile(message);
    }

    @Override
    public boolean deleteMessage(String messageId) {
        Message message = messagesById.get(messageId);
        messagesById.remove(messageId);
        return deleteMessageFromFile(message);
    }

}
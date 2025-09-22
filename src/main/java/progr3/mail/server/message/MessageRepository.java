package progr3.mail.server.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Message;

public class MessageRepository implements IMessageRepository {

    private final Map<String, Message> messagesById = new HashMap<>();
    private final JsonFileHandler jsonFileHandler = new JsonFileHandler();
    private static final String filePath = "data/messages.json";

    public MessageRepository() {
        this.loadMessages();
    }

    private void loadMessages() {
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

    private boolean saveMessages(List<Message> messages) {
        try {
            jsonFileHandler.saveToFile(messages, filePath);
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
        return saveMessages(new ArrayList<>(messagesById.values()));
    }

    @Override
    public boolean deleteMessage(String messageId) {
        messagesById.remove(messageId);
        return saveMessages(new ArrayList<>(messagesById.values()));
    }

}
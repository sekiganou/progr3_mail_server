package progr3.mail.server.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.io.IJsonFileHandler;
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
                messagesByUserId.computeIfAbsent(message.getSenderUserGUID(), k -> new ArrayList<>())
                        .add(message);
                messagesById.put(message.getGuid(), message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> getAllUserMessages(String userId) {
        return messagesByUserId.getOrDefault(userId, new ArrayList<>());
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

        try {
            messagesById.put(message.getGuid(), message);
            messagesByUserId.computeIfAbsent(message.getSenderUserGUID(), k -> new ArrayList<>())
                    .add(message);
            jsonFileHandler.saveToFile(message, filePath, Message.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteMessage(String messageId) {
        if (messageId == null)
            return false;

        var message = messagesById.get(messageId);

        if (message == null)
            return false;

        messagesById.remove(messageId);

        try {
            jsonFileHandler.removeFromFile(message, filePath, Message.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
package progr3.mail.server.message;

import java.util.List;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Message;

public class IO {
    private final JsonFileHandler jsonFileHandler = new JsonFileHandler();
    private static final String filePath = "data/users.json";

     public List<Message> loadMessages() {
        try {
            return jsonFileHandler.loadFromFile(filePath, Message.class);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean saveUsers(List<Message> messages) {
        try {
            jsonFileHandler.saveToFile(messages, filePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

package progr3.mail.server.user;

import java.util.List;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.User;

public class IO {
    private final JsonFileHandler jsonFileHandler = new JsonFileHandler();
    private static final String filePath = "data/users.json";

    public List<User> loadUsers() {
        try {
            return jsonFileHandler.loadFromFile(filePath, User.class);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean saveUsers(List<User> users) {
        try {
            jsonFileHandler.saveToFile(users, filePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

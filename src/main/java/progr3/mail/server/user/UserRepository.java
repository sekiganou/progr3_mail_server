package progr3.mail.server.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.User;

public class UserRepository implements IUserRepository {

    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, User> usersById = new HashMap<>();
    private final JsonFileHandler jsonFileHandler = new JsonFileHandler();
    private static final String filePath = "data/users.json";

    public UserRepository() {
        this.loadUsers();
    }

    private void loadUsers() {
        List<User> users = List.of();
        try {
            users = jsonFileHandler.loadFromFile(filePath, User.class);
            for (User user : users) {
                usersByEmail.put(user.getEmail(), user);
                usersById.put(user.getGuid(), user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
        return List.copyOf(usersById.values());
    }

    @Override
    public User getUserDetailsById(String userId) {
        return usersById.get(userId);
    }

    @Override
    public User getUserDetailsByEmail(String email) {
        return usersByEmail.get(email);
    }

}

package progr3.mail.server.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.io.IJsonFileHandler;
import progr3.mail.server.model.User;

public class UserRepository implements IUserRepository {

    private final IJsonFileHandler jsonFileHandler;
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, User> usersById = new HashMap<>();
    private final String filePath;

    public UserRepository(IJsonFileHandler jsonFileHandler, String filePath) {
        this.jsonFileHandler = jsonFileHandler;
        this.filePath = filePath;
        this.loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        List<User> users = new ArrayList<>();
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
    public boolean saveUser(User user) {
        if (usersById.get(user.getGuid()) != null) {
            return false;
        }

        try {
            usersById.put(user.getGuid(), user);
            usersByEmail.put(user.getEmail(), user);
            jsonFileHandler.saveToFile(user, filePath, User.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public List<User> getAllUsers() {
        return List.copyOf(usersById.values());
    }

    @Override
    public User getUserById(String userId) {
        return usersById.get(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        return usersByEmail.get(email);
    }

}

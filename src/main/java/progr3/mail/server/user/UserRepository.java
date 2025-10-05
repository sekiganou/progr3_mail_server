package progr3.mail.server.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
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
    public String saveUser(User user) throws BadRequestException, IOException {
        var validationError = UserValidator.isValidUser(user);
        if (validationError != null)
            throw new BadRequestException(validationError);

        if (usersById.get(user.getGuid()) != null) {
            return user.getGuid();
        }

        usersById.put(user.getGuid(), user);
        usersByEmail.put(user.getEmail(), user);

        jsonFileHandler.saveToFile(user, filePath, User.class);
        return user.getGuid();

    }

    @Override
    public List<User> getAllUsers() {
        return usersById.isEmpty() ? new ArrayList<>() : List.copyOf(usersById.values());
    }

    @Override
    public User getUserById(String userId) throws BadRequestException, UserNotFoundException {
        if (userId == null || userId.isEmpty())
            throw new BadRequestException("User ID cannot be null");
        var user = usersById.get(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) throws BadRequestException, UserNotFoundException {
        if (email == null || email.isEmpty())
            throw new BadRequestException("Email cannot be null");

        var user = usersByEmail.get(email);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

}

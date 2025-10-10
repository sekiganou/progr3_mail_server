package progr3.mail.server.user;

import java.util.ArrayList;
import java.util.List;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.log.ILogger;
import progr3.mail.server.model.User;

public class UserService {
    private IUserRepository userRepository;
    private ILogger logger;

    public UserService(IUserRepository userRepository, ILogger logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    public User login(String email) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Attempting login for email: " + email);

        User user = userRepository.getUserByEmail(email);

        return user;
    }

    public List<User> getUsersByIds(List<String> userIds) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Retrieving users by IDs: " + userIds);
        var users = new ArrayList<User>();

        for (var userId : userIds) {
            users.add(userRepository.getUserById(userId));
        }

        return users;
    }

    public User getUserById(String userId) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Retrieving user by ID: " + userId);
        return userRepository.getUserById(userId);
    }

    public User getUserByEmail(String email) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Retrieving user by email: " + email);

        return userRepository.getUserByEmail(email);
    }
}

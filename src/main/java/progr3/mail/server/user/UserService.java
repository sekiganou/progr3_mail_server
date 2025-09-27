package progr3.mail.server.user;

import progr3.mail.server.log.ILogger;
import progr3.mail.server.model.User;

public class UserService {
    private IUserRepository userRepository;
    private ILogger logger;

    public UserService(IUserRepository userRepository, ILogger logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    public boolean login(String email) {
        logger.logInfo("Attempting login for email: " + email);

        var user = userRepository.getUserByEmail(email);
        return user != null;
    }

    public User getUserById(String userId) {
        logger.logInfo("Retrieving user by ID: " + userId);
        return userRepository.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        logger.logInfo("Retrieving user by email: " + email);
        return userRepository.getUserByEmail(email);
    }
}

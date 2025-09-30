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
        boolean loggedIn = user != null;

        if (loggedIn) {
            logger.logInfo("Login successful for email: " + email);
        } else {
            logger.logError("Login failed for email: " + email, null);
        }

        return loggedIn;
    }

    public User getUserById(String userId) {
        logger.logInfo("Retrieving user by ID: " + userId);
        var user = userRepository.getUserById(userId);

        if (user == null) {
            logger.logError("User not found with ID: " + userId, null);
            return null;
        }

        return user;
    }

    public User getUserByEmail(String email) {
        logger.logInfo("Retrieving user by email: " + email);

        var user = userRepository.getUserByEmail(email);

        if (user == null) {
            logger.logError("User not found with email: " + email, null);
            return null;
        }

        return user;
    }
}

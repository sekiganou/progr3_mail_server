package progr3.mail.server.user;

import progr3.mail.server.app.ResponseConstructor;
import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.log.ILogger;
import progr3.mail.server.model.Response;
import progr3.mail.server.model.User;
import progr3.mail.server.model.MailResponse.LoginBodyOut;
import progr3.mail.server.model.Response.Result;
import progr3.mail.server.model.Response.Status;

public class UserService {
    private IUserRepository userRepository;
    private ILogger logger;

    public UserService(IUserRepository userRepository, ILogger logger) {
        this.userRepository = userRepository;
        this.logger = logger;
    }

    public User login(String email) throws BadRequestException, UserNotFoundException {
        logger.logInfo("Attempting login for email: " + email);

        var user = userRepository.getUserByEmail(email);

        // var response = new Response();
        // var loginBodyOut = new LoginBodyOut();

        // if (user != null) {
        // logger.logInfo("Login successful for email: " + email);
        // loginBodyOut.setGuid(user.getGuid());
        // response = ResponseConstructor.success("Login Successful", loginBodyOut);
        // } else {
        // logger.logError("Login failed for email: " + email, null);
        // response = ResponseConstructor.unauthorized("Login Failed: User not found",
        // email);
        // }

        return user;
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

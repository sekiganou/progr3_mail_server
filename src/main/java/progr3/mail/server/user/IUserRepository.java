package progr3.mail.server.user;

import java.io.IOException;
import java.util.List;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.model.User;

public interface IUserRepository {
    List<User> getAllUsers();

    User getUserById(String userId) throws BadRequestException, UserNotFoundException;

    User getUserByEmail(String email) throws BadRequestException, UserNotFoundException;

    String saveUser(User user) throws BadRequestException, IOException;
}

package progr3.mail.server.user;

import java.util.List;

import progr3.mail.server.model.User;

public interface IUserRepository {
    List<User> getAllUsers();

    User getUserDetailsById(String userId);

    User getUserDetailsByEmail(String email);
}

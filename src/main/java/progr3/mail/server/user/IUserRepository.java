package progr3.mail.server.user;

import java.util.List;

import progr3.mail.server.model.User;

public interface IUserRepository {
    List<User> getAllUsers();

    User getUserById(String userId);

    User getUserByEmail(String email);

    boolean saveUser(User user);
}

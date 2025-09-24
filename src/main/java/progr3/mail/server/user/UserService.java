package progr3.mail.server.user;

import progr3.mail.server.model.User;

public class UserService {
    private IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String email) {
        var user = userRepository.getUserByEmail(email);
        return user != null;
    }

    public User getUserById(String userId) {
        return userRepository.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
}

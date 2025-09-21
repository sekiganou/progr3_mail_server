package progr3.mail.server.user;

import java.util.Map;
import java.util.stream.Collectors;

import progr3.mail.server.model.User;

public class Model {
    private final IO io;
    private static Map<String, User> users = Map.of();

    public Model() {
        this.io = new IO();
        Model.users = io.loadUsers().stream().collect(Collectors.toMap(User::getEmail, user -> user));
    }

    public User getUserByEmail(String email) {
        return users.get(email);
    }

}

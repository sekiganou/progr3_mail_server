package progr3.mail.server.user;

import progr3.mail.server.model.User;

public class UserConstructor {
    public static User create(String email, String password, String name) {
        var user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}

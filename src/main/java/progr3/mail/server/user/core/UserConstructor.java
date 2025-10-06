package progr3.mail.server.user.core;

import java.util.UUID;

import progr3.mail.server.model.User;

public class UserConstructor {
    public static User create(String email, String name) {
        var user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setGuid(UUID.randomUUID().toString());
        return user;
    }

}

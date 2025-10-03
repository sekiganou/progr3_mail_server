package progr3.mail.server.app;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ActiveUsers {
    private final Map<String, Socket> activeUsers = new HashMap<>();

    public synchronized void addUser(String userId, Socket socket) {
        activeUsers.put(userId, socket);
    }

    public synchronized void removeUser(String userId) {
        activeUsers.remove(userId);
    }

    public synchronized Socket getUserSocket(String userId) {
        return activeUsers.get(userId);
    }

}

package progr3.mail.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import javafx.application.Application;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.message.MessageConstructor;
import progr3.mail.server.message.MessageRepository;

public class Launcher {
    public static void main(String[] args) {
        // Application.launch(HelloApplication.class, args);
        // var logger = new Logger();
        // var server = new Server(logger);

        // server.start();
        var jsonFileHandler = new JsonFileHandler();
        var repository = new MessageRepository(jsonFileHandler, "data/test/messages.json");

        var testMessage1 = MessageConstructor.create("user-1",
                Arrays.asList("user-2@test.com"), "Test Subject 1", "Test Body 1");
        var testMessage2 = MessageConstructor.create("user-2",
                Arrays.asList("user-1@test.com"), "Test Subject 2", "Test Body 2");

        repository.saveMessage(testMessage1);
        repository.saveMessage(testMessage2);

        System.out.println("Messages for user-1:");
        var messages = repository.getAllUserMessages("user-1");
        messages.forEach(msg -> System.out.println(msg.getSubject()));
    }
}

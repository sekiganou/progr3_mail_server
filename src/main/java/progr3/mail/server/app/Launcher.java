package progr3.mail.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.message.IMessageRepository;
import progr3.mail.server.message.MessageRepository;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.message.core.MessageConstructor;
import progr3.mail.server.user.IUserRepository;
import progr3.mail.server.user.UserRepository;
import progr3.mail.server.user.UserService;
import progr3.mail.server.user.core.UserConstructor;

public class Launcher {
    public static void main(String[] args) {
        // Application.launch(HelloApplication.class, args);
        var jsonFileHandler = new JsonFileHandler();
        var logger = new Logger(LogLevelEnum.INFO, "data/prod/logs.json", true, true, jsonFileHandler);
        var messageRepo = new MessageRepository(jsonFileHandler, "data/prod/messages.json");
        var userRepo = new UserRepository(jsonFileHandler, "data/prod/users.json");
        var messageService = new MessageService(messageRepo, userRepo, logger);
        var userService = new UserService(userRepo, logger);
        var seeder = new Seeder(logger);
        seeder.seedUsers(userRepo);
        var server = new Server(new ActiveUsers(), userService, messageService, logger);

        // try {
        // messageService.sendMessage("cb0466b6-348b-444f-9ee4-189573a606b5",
        // List.of("alessio-bagno@unito.com"),
        // "Test Subject 1", "Test Body 1");

        // messageService.sendMessage("cb0466b6-348b-444f-9ee4-189573a606b5",
        // List.of("alessio-bagno@unito.com"),
        // "Test Subject 2", "Test Body 2");

        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true); // Make it a daemon thread so it stops when
        serverThread.start();

        Application.launch(LogViewApplication.class, args);
    }
}

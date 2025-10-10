package progr3.mail.server.app;

import javafx.application.Application;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.message.MessageRepository;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.user.UserRepository;
import progr3.mail.server.user.UserService;

public class Launcher {
    public static void main(String[] args) {
        var jsonFileHandler = new JsonFileHandler();
        var logger = new Logger(LogLevelEnum.INFO, "data/prod/logs.json", true, true, jsonFileHandler);
        var messageRepo = new MessageRepository(jsonFileHandler, "data/prod/messages.json");
        var userRepo = new UserRepository(jsonFileHandler, "data/prod/users.json");
        var messageService = new MessageService(messageRepo, userRepo, logger);
        var userService = new UserService(userRepo, logger);
        var seeder = new Seeder(logger);
        seeder.seedUsers(userRepo);
        var server = new Server(userService, messageService, logger);

        MailServerApplication.setServer(server);

        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true); // Make it a daemon thread so it stops when the main thread exits
        serverThread.start();

        Application.launch(MailServerApplication.class, args);
    }
}

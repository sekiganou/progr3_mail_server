package progr3.mail.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

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
        server.start();
    }
}

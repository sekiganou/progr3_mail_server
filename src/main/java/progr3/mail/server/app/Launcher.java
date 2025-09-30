package progr3.mail.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import javafx.application.Application;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.message.IMessageRepository;
import progr3.mail.server.message.MessageConstructor;
import progr3.mail.server.message.MessageRepository;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.user.IUserRepository;
import progr3.mail.server.user.UserConstructor;
import progr3.mail.server.user.UserRepository;
import progr3.mail.server.user.UserService;

public class Launcher {
    public static void main(String[] args) {
        // Application.launch(HelloApplication.class, args);
        // var logger = new Logger();
        // var server = new Server(logger);

        // server.start();
        var jsonFileHandler = new JsonFileHandler();
        var messageRepo = new MessageRepository(jsonFileHandler, "data/test/messages.json");
        var userRepo = new UserRepository(jsonFileHandler, "data/test/users.json");

        var testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        var testUser2 = UserConstructor.create("user-2@test.com", "user-2");

        var logger = new Logger(
                LogLevelEnum.INFO, "data/test/logs.json", true, true,
                jsonFileHandler);
        var messageService = new MessageService(messageRepo, userRepo, logger);
        var userService = new UserService(userRepo, logger);

        userRepo.saveUser(testUser1);
        userRepo.saveUser(testUser2);

        logger.startScope();
        userService.login(testUser1.getEmail());
        logger.endScope();

        logger.startScope();
        messageService.getAllUserMessages(testUser1.getGuid());
        logger.endScope();

        logger.startScope();
        messageService.sendMessage(testUser1.getGuid(), Arrays.asList(testUser2.getEmail()),
                "Hello", "This is a test message.");
        logger.endScope();

    }
}

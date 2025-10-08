package progr3.mail.server.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.user.UserService;

public class Server implements Runnable {

    private ILogger logger;
    private MessageService messageService;
    private UserService userService;

    private final int N_WORKERS = 10;
    private final int PORT = 8080;

    public Server(ActiveUsers activeUsers, UserService userService, MessageService messageService, ILogger logger) {
        this.logger = logger;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void run() {
        logger.startScope();
        logger.logInfo("Starting Mail Server...");
        logger.endScope();

        Executor pool = Executors.newFixedThreadPool(N_WORKERS);
        var activeUsers = new ActiveUsers();

        try (var serverSocket = new java.net.ServerSocket(PORT)) {
            logger.startScope();
            logger.logInfo("Mail server started on port " + PORT);
            logger.endScope();

            while (true) {
                var clientSocket = serverSocket.accept();
                logger.startScope();
                logger.logInfo("Client connected: " + clientSocket.getInetAddress());
                logger.endScope();

                pool.execute(new ClientHandler(clientSocket, logger, activeUsers, userService, messageService));
            }
        } catch (Exception e) {
            logger.startScope();
            logger.logError("Error in server socket operations");
            logger.endScope();
        } finally {
            logger.startScope();
            logger.logInfo("Shutting down Mail Server...");
            logger.endScope();
        }
    }

}

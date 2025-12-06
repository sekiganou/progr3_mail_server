package progr3.mail.server.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.user.UserService;

public class Server implements Runnable {

    private ILogger logger;
    private MessageService messageService;
    private UserService userService;

    private final int N_WORKERS = 10;
    private final int TASK_TIMEOUT_S = 5;
    private final int PORT = 8080;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public Server(UserService userService, MessageService messageService, ILogger logger) {
        this.logger = logger;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void run() {
        logger.startScope();
        logger.logInfo("Starting Mail Server...");
        logger.endScope();

        pool = Executors.newFixedThreadPool(N_WORKERS);

        try (var serverSocket = new java.net.ServerSocket(PORT)) {
            this.serverSocket = serverSocket;

            logger.startScope();
            logger.logInfo("Mail server started on port " + PORT);
            logger.endScope();

            while (running) {
                try {
                    var clientSocket = serverSocket.accept();
                    var clientHandler = new ClientHandler(clientSocket, logger, userService, messageService);
                    pool.execute(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        logger.startScope();
                        logger.logError("Error accepting client: " + e.getMessage());
                        logger.endScope();
                    }
                }
            }

        } catch (Exception e) {
            logger.startScope();
            logger.logError("Error in server socket operations");
            logger.endScope();
        }
    }

    public void stopServer() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close(); // This unblocks accept()
            } catch (IOException e) {
                logger.startScope();
                logger.logError("Error closing server socket: " + e.getMessage());
                logger.endScope();
            }
        }

        if (pool != null && !pool.isShutdown()) {
            try {
                pool.awaitTermination(TASK_TIMEOUT_S, TimeUnit.SECONDS);
                pool.shutdown();
            } catch (InterruptedException e) {
                logger.startScope();
                logger.logError("Error shutting down thread pool: " + e.getMessage());
                logger.endScope();
            }

        }

        logger.startScope();
        logger.logInfo("Server stop requested.");
        logger.endScope();
    }

}

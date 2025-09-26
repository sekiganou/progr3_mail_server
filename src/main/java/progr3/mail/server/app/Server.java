package progr3.mail.server.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import progr3.mail.server.log.ILogger;

public class Server {

    private final ILogger logger;

    private final int N_WORKERS = 10;
    private final int PORT = 8080;

    public Server(ILogger logger) {
        this.logger = logger;
    }

    public void start() {
        logger.logInfo("Starting Mail Server...");

        Executor pool = Executors.newFixedThreadPool(N_WORKERS);

        logger.logInfo("Mail server started on port " + PORT);
        try (var serverSocket = new java.net.ServerSocket(PORT)) {
            while (true) {
                var clientSocket = serverSocket.accept();
                logger.logInfo("Client connected: " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket, logger));
            }
        } catch (Exception e) {
            logger.logError("Error in server socket operation", e);
        }
    }

}

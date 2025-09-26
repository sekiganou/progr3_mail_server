package progr3.mail.server.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import progr3.mail.server.log.ILogger;

public class ClientHandler implements Runnable {

    private ILogger logger;
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket, ILogger logger) {
        this.logger = logger;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        byte[] inputStreamBytes;
        try {
            InputStream inputStream = clientSocket.getInputStream();
            inputStreamBytes = inputStream.readAllBytes();
            clientSocket.close();
        } catch (IOException e) {
            logger.logError("Error handling client connection", e);
            return;
        }

        String request = new String(inputStreamBytes);

        logger.logInfo("Received request: " + request);

        String[] parts = request.split("\n", 2); // split into command and optional data
        String command = parts[0].trim();
        String data = parts.length > 1 ? parts[1] : null;

        switch (command) {
            default:
                break;
        }

    }

}

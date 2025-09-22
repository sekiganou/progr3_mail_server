package progr3.mail.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Application.launch(HelloApplication.class, args);
        var logger = new Logger();
        var server = new Server(logger);

        server.start();
    }
}

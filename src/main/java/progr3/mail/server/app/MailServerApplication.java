package progr3.mail.server.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MailServerApplication extends Application {
    private static Server server;

    public static void setServer(Server server) {
        MailServerApplication.server = server;
    }

    @Override
    public void stop() {
        System.out.println("JavaFX application stopping...");
        server.stopServer(); // Gracefully stop the server
        Platform.exit();
        System.exit(0); // Ensure the JVM shuts down
    }

    @Override
    public void start(Stage stage) throws java.io.IOException {
        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                MailServerApplication.class.getResource("log-view.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Mail Server Logs");
        stage.setScene(scene);
        stage.show();
    }

}

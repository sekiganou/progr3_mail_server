package progr3.mail.server.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class LogViewApplication extends Application {
    @Override
    public void start(Stage stage) throws java.io.IOException {
        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                LogViewApplication.class.getResource("log-view.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Mail Server Logs");
        stage.setScene(scene);
        stage.show();
    }

}

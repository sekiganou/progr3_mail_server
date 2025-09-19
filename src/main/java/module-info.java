module progr3.mail.server.app {
    requires javafx.controls;
    requires javafx.fxml;


    opens progr3.mail.server.app to javafx.fxml;
    exports progr3.mail.server.app;
}
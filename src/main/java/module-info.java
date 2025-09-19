module progr3.mail.server.progr3_mail_server {
    requires javafx.controls;
    requires javafx.fxml;


    opens progr3.mail.server.progr3_mail_server to javafx.fxml;
    exports progr3.mail.server.progr3_mail_server;
}
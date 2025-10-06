module progr3.mail.server.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.compiler;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires atlantafx.base;
    requires javafx.graphics;

    opens progr3.mail.server.app to javafx.fxml;

    exports progr3.mail.server.app;
    exports progr3.mail.server.model;
}
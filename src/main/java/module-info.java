module com.app.studysnap {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires java.dotenv;
    requires java.net.http;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client;
    requires com.google.api.services.oauth2;
    requires com.google.gson;

    exports com.app.studysnap;
    opens com.app.studysnap to javafx.fxml;
    opens com.app.studysnap.controllers to javafx.fxml;
}
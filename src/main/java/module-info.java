module com.example.cab302_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.oauth2;
    requires jdk.httpserver;
    requires org.apache.pdfbox;
    requires java.net.http;


    opens com.app.studysnap to javafx.fxml;
    exports com.app.studysnap;
    exports com.app.studysnap.controllers;
    opens com.app.studysnap.controllers to javafx.fxml;
    exports com.app.studysnap.model;
    opens com.app.studysnap.model to javafx.fxml;
    exports com.app.studysnap.services;
    opens com.app.studysnap.services to javafx.fxml;
}
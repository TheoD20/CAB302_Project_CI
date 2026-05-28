/**
 * StudySnap application module.
 * <p>
 * Declares JavaFX UI, data-access, service layers and opens controller packages for FXML injection.
 * </p>
 * <p>
 *     * Controllers: JavaFX backend (bind UI to services)
 * </p>
 * <p>
 *     * Model: Object classes and DAOs
 * </p>
 * <p>
 *     * Services: Internal helpers and external API service handlers
 * </p>
 */
module com.app.studysnap {
    requires javafx.controls;
    requires javafx.fxml;
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
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;
    requires jbcrypt;

    exports com.app.studysnap;
    exports com.app.studysnap.controllers;
    exports com.app.studysnap.model;
    exports com.app.studysnap.services;

    opens com.app.studysnap to javafx.fxml;
    opens com.app.studysnap.controllers to javafx.fxml;
    opens com.app.studysnap.model to javafx.fxml;
    opens com.app.studysnap.services to javafx.fxml;
}

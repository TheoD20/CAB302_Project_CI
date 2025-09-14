package com.app.studysnap.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

public class GoogleAuthService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Logs a Google user by https call and API key
    public Userinfo login() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Get key
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/client_secret.json")))
        );

        // Build call
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Arrays.asList("https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile"))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Get user info
        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("StudySnap")
                .build();

        return oauth2.userinfo().get().execute();
    }
}
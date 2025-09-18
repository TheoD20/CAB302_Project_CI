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
import java.util.Arrays;

public class GoogleAuthService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Logs a Google user by https call and API key
    public Userinfo login() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Get key
        String clientId = System.getenv("STUDYSNAP_GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("STUDYSNAP_GOOGLE_CLIENT_SECRET");

        GoogleClientSecrets clientSecrets;
        if (clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank()) {
            var details = new GoogleClientSecrets.Details();
            details.setClientId(clientId);
            details.setClientSecret(clientSecret);

            // Optional: if you want to set a redirect explicitly via ENV (usually not needed for Desktop)
            String redirect = "http://localhost";
            details.setRedirectUris(java.util.List.of(redirect));

            clientSecrets = new GoogleClientSecrets().setInstalled(details);
        } else {
            throw new IllegalStateException("Missing STUDYSNAP_GOOGLE_CLIENT_ID/STUDYSNAP_GOOGLE_CLIENT_SECRET environment variables.");
        }

        int port = parsePort(System.getenv("STUDYSNAP_GOOGLE_OAUTH_PORT"), 8888);
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(port).build();

        // Build call
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Arrays.asList("https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile"))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Get user info
        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("StudySnap")
                .build();

        return oauth2.userinfo().get().execute();
    }

    private static int parsePort(String s, int def) {
        try { return (s == null || s.isBlank()) ? def : Integer.parseInt(s); }
        catch (NumberFormatException e) { return def; }
    }
}
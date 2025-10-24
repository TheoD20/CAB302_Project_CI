package com.app.studysnap.auth;

import com.app.studysnap.exceptions.ExternalServiceException;
import com.app.studysnap.exceptions.ValidationException;
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

import static com.app.studysnap.services.TextParser.isBlank;

/**
 * Handles the Google OAuth 2.0 desktop flow to retrieve the authenticated user's profile
 * (email and basic profile info) using the Google OAuth2 API.
 * <p>
 * This service launches a local HTTP receiver to complete the installed-app flow,
 * exchanges the authorization code for credentials, and then requests {@link Userinfo}.
 * </p>
 */
public class GoogleAuthService {

    /**
     * Default constructor:
     * Creates a new {@code GoogleAuthService}.
     */
    public GoogleAuthService() {}

    /**
     * JSON factory used by the Google client library for request/response.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Performs the OAuth 2.0 login flow and returns the authenticated Google {@link Userinfo}.
     * @return The authenticated user's {@link Userinfo}.
     * @throws Exception if the OAuth exchange fails or is cancelled
     * @throws ValidationException if the required environment variables are missing/blank.
     * @throws ExternalServiceException if the Google transport/flow or userinfo call fails.
     */
    public Userinfo login() throws Exception {
        try {
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
                throw new ValidationException("Missing STUDYSNAP_GOOGLE_CLIENT_ID/STUDYSNAP_GOOGLE_CLIENT_SECRET environment variables.");
            }

            int port = parsePort(System.getenv("STUDYSNAP_GOOGLE_OAUTH_PORT"));
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
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to complete Google OAuth flow or fetch user info.", e);
        }
    }

    /**
     * Parses a port number from an environment string, falling back to a default if blank/invalid.
     *
     * @param s The string containing the port number (can be blank).
     * @return The parsed port number, or {@code def} if input is blank/invalid.
     */
    private static int parsePort(String s) {
        try {
            return isBlank(s) ? 8888 : Integer.parseInt(s);
        }
        catch (NumberFormatException e) { return 8888; }
    }
}
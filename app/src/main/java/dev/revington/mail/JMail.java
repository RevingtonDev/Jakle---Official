package dev.revington.mail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session; 
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JMail {

    private final String APPLICATION = "jakle";
    private final String APPLICATION_NAME = "Jakle @Official";
    private final String APPLICATION_EMAIL = "noreply.jakle.official@gmail.com";
    private final String SUPPORT_EMAIL = "michael.revington@gmail.com";
    private final String TOKEN_DIRECTORY = "token";
    private final String CREDENTIALS_FILE = "/mail/secrets/client_secrets.json";

    private final Logger logger = Logger.getLogger(JMail.class.getName());

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);

    public Credential getCredentials(final NetHttpTransport transport)
            throws IOException {
        InputStream is = JMail.class.getResourceAsStream(CREDENTIALS_FILE);
        if (is == null) {
            throw new FileNotFoundException();
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(is));
        GoogleAuthorizationCodeFlow codeFlow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKEN_DIRECTORY)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(9000) 
                .build();
        
        return new AuthorizationCodeInstalledApp(codeFlow, receiver).authorize("user");
    }

    public boolean sendMail(String to, String subject, String content)
            throws GeneralSecurityException, IOException, MessagingException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
        .createScoped(GmailScopes.GMAIL_COMPOSE);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        Gmail service = new Gmail.Builder(transport, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION)
                .build();
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(APPLICATION_EMAIL, APPLICATION_NAME));
        email.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setReplyTo(new Address[]{new InternetAddress(SUPPORT_EMAIL)});
        email.setContent(content, "text/html; charset=utf-8");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
        
        Message message = new Message();
        message.setRaw(encodedEmail);
        List<String> labels = new ArrayList<>();
        labels.add("IMPORTANT");
        message.setLabelIds(labels);
        
        try {
            service.users().messages().send("me", message).execute();
            return true;
        } catch (GoogleJsonResponseException e) {
            Logger.getLogger(JMail.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

}

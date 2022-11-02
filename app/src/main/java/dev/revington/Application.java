package dev.revington;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import dev.revington.mail.JMail;
import dev.revington.repository.*;
import dev.revington.variables.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.core.env.Environment;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private JMail mailService;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        userRepository.findAll().forEach((user) -> {
            user.setActivity(Parameter.OFFLINE);
            user.setSocketId("");
            user.setActive("");
            userRepository.save(user);
        });

        mailService.getCredentials(GoogleNetHttpTransport.newTrustedTransport());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

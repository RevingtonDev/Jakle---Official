package dev.revington.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.revington.entity.Notification;
import dev.revington.entity.Token;
import dev.revington.entity.User;
import dev.revington.mail.JMail;
import dev.revington.repository.*;
import dev.revington.util.Security;
import dev.revington.util.TokenUtil;
import dev.revington.variables.Parameter;
import dev.revington.variables.StatusHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

import net.minidev.json.parser.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class Account {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AtomicNotificationRepository atomicNotificationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private JMail mailService;

    @Value("${imagehost.key}")
    private String key;

    @Value("${imagehost.url}")
    private String hostUrl;

    private Logger logger = Logger.getLogger(Account.class.getName());

    @PostMapping("/notifications")
    public ResponseEntity<JSONObject> getNotifications(HttpServletRequest req,
            @RequestParam(required = false, defaultValue = "true") String all,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "15") int limit,
            @RequestParam(required = false, defaultValue = "1") int filter) {
        if (page < 0) {
            page = 0;
        } else {
            page--;
        }
        if (limit < 15) {
            limit = 15;
        }
        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        String id = req.getAttribute(Parameter.CLIENT_ID).toString();
        if (all.equals("true")) {
            Slice<Notification> notifications;
            switch (filter) {
                case 2:
                    notifications = notificationRepository.findAllByOwnerWithFilter(
                            id, Parameter.Category.FRIEND_REQUEST.label, Pageable.ofSize(limit).withPage(page)
                    );
                    break;
                case 1:
                default:
                    notifications = notificationRepository.findAllByOwner(id, Pageable.ofSize(limit).withPage(page));
            }
            result.appendField(Parameter.ELEMENTS, notifications.getNumberOfElements());
            result.appendField(Parameter.PAGES, (notifications.getNumberOfElements() / limit)
                    + (notifications.getNumberOfElements() % limit > 0 ? 1 : 0));
            result.appendField(Parameter.PAGE, page);
            result.appendField(Parameter.RESULTS, notifications.toList().toArray());
            // notificationRepository.updateAllFromOwner(req.getAttribute(Parameter.CLIENT_ID).toString(),
            // true);
            atomicNotificationRepository.updateUnreadNoifications(id);
        } else {
            List<Notification> notifications = notificationRepository
                    .findUnread(req.getAttribute(Parameter.CLIENT_ID).toString());
            result.put(Parameter.RESULTS, notifications);
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PostMapping("/info")
    public ResponseEntity<JSONObject> getInfo(HttpServletRequest req,
            @RequestParam(required = false, defaultValue = "") String id) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        Optional<User> user;
        if (id.equals("")) {
            user = userRepository.findPersonal(userId);
        } else {
            user = userRepository.findInfo(id);
        }
        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        if (!user.isPresent()) {
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);
        }
        result.put(Parameter.RESULTS, user.get());
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = userRepository.findById(req.getAttribute(Parameter.CLIENT_ID).toString()).get();
        user.setActivity(Parameter.OFFLINE);
        user.setSocketId("");

        tokenRepository.deleteByToken(req.getAttribute(Parameter.AUTH).toString());
        resp.sendRedirect("/");
    }

    @PostMapping("/update")
    public ResponseEntity<JSONObject> updateInfo(HttpServletRequest req, @RequestBody User update) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setName(update.getName());
            user.setDateOfBirth(update.getDateOfBirth());
            userRepository.save(user);
        }

        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        result.put(Parameter.RESULTS, userRepository.findPersonal(userId).get());

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PostMapping("/reset-link")
    public ResponseEntity<JSONObject> sendResetLink(HttpServletRequest req, @RequestParam(required = false, defaultValue = "") String email) {
        Object userId = null;
        User user = null;
        Token token = null;
        if ((userId = req.getAttribute(Parameter.CLIENT_ID)) != null) {
            user = userRepository.findById(userId.toString()).get();
        } else if (!email.equals("")) {
            email = Security.normalizeEmail(email);
            Optional<User> optional = userRepository.findByEmail(email);
            if (optional.isPresent()) {
                user = optional.get();
            }
        }
        if (user == null || user.getValidity() != Parameter.ACTIVATED || user.getStatus() != Parameter.ACTIVE) {
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);
        }

        token = tokenRepository.findTokenByOwner(user.getId(), Parameter.GRANT_RESET);
        long now = new Date().getTime();
        if (token != null && token.getCreated() + (1000L * 60 * 15) > now) {
            return new ResponseEntity<>(StatusHandler.E1021, HttpStatus.ACCEPTED);
        }

        try {
            if (token == null) {
                token = TokenUtil.generateToken(user, Parameter.GRANT_RESET, (1000L * Parameter.RESET_TOKEN_TIMEOUT));
            } else {
                token = TokenUtil.regenToken(user, token, (1000L * Parameter.RESET_TOKEN_TIMEOUT));
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);
        }

        tokenRepository.save(token);
        if (!Security.sendReset(mailService, user, token, user.getName().split(" ")[0], req.getRequestURL().toString().replace(req.getRequestURI(), ""))) {
            tokenRepository.delete(token);
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(StatusHandler.S200, HttpStatus.ACCEPTED);
    }

    @PostMapping("/upload")
    public ResponseEntity<JSONObject> uploadFile(HttpServletRequest req, @RequestParam MultipartFile file) throws IOException, URISyntaxException {
        URL url = new URL(hostUrl);
        User user = userRepository.findById(req.getAttribute(Parameter.CLIENT_ID).toString()).get();

        if (user.getActivity() != Parameter.ONLINE)
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);

        ArrayList<NameValuePair> values = new ArrayList<>();
        values.add(new BasicNameValuePair("action", "upload"));
        values.add(new BasicNameValuePair("source", Base64.getEncoder().encodeToString(file.getBytes())));
        values.add(new BasicNameValuePair("key", key));
        values.add(new BasicNameValuePair("format", "json"));

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().build()).build();
        HttpPost post = new HttpPost(url.toURI());
        post.setEntity(new UrlEncodedFormEntity(values));
        CloseableHttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() != 200) {
            return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);
        } else {
            String content = new String(response.getEntity().getContent().readAllBytes());
            JSONObject object = new ObjectMapper().readValue(content, JSONObject.class);
            String img_url = ((LinkedHashMap)object.get("image")).get("display_url").toString();

            if (img_url == null || img_url.equals(""))
                return new ResponseEntity<>(StatusHandler.E500, HttpStatus.ACCEPTED);

            JSONObject result = (JSONObject) StatusHandler.S200.clone();
            user.setProfilePic(img_url);
            userRepository.save(user);
            result.put(Parameter.RESULTS, img_url);
            return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
        }
    }
}

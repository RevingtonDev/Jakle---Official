package dev.revington.api;

import com.google.common.collect.HashBiMap;
import dev.revington.entity.Friend;
import dev.revington.entity.Notification;
import dev.revington.entity.Request;
import dev.revington.entity.User;
import dev.revington.repository.FriendRepository;
import dev.revington.repository.MessageRepository;
import dev.revington.repository.NotificationRepository;
import dev.revington.repository.RequestRepository;
import dev.revington.repository.TokenRepository;
import dev.revington.repository.UserRepository;
import dev.revington.variables.Parameter;
import dev.revington.variables.StatusHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social")
public class Social {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @PostMapping("/friend")
    public ResponseEntity<JSONObject> acceptFriend(HttpServletRequest req, @RequestParam String id, @RequestParam String add) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        Optional<Request> optionalRequest = requestRepository.findByFrom(id, userId);
        if (optionalRequest.isEmpty()) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.FORBIDDEN);
        }

        Request request = optionalRequest.get();
        requestRepository.delete(request);
        notificationRepository.onAcceptFriend(userId, id);

        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        if (add.toLowerCase().equals("yes")) {
            Friend friend = new Friend();
            friend.setFriends(request.getFrom() + ":" + request.getTo());
            friend.setFirst(request.getFrom());
            friend.setSecond(request.getTo());
            friend.setTimestamp(new Date().getTime());
            friendRepository.save(friend);

            Notification notification = new Notification();
            notification.setCategory(Parameter.Category.REQUEST_ACCEPT.label);
            notification.setNotification((Pair.of(Parameter.REQUEST, userId)));
            notification.setOwner(id);
            notification.setRead(false);
            notification.setTime(new Date().getTime());
            notificationRepository.save(notification);

            Optional<User> optional = userRepository.findById(id);
            User user = null; 
            if (optional.isPresent() && (user = optional.get()).getActivity() == Parameter.ONLINE) {
                messageTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_NOTIFY).appendField(Parameter.DATA, true));
                messageTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_ADD_USER).appendField(Parameter.WS_DATA, userId));
            } 
            result.put(Parameter.RESULTS, userRepository.findInfo(id).get());
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PutMapping("/friend")
    public ResponseEntity<JSONObject> addFriend(HttpServletRequest req, @RequestParam String id) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        if (userId.equals(id) || userRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.BAD_REQUEST);
        }

        Optional<Friend> optional = friendRepository.findByFriend(userId + ":" + id, id + ":" + userId);
        if (optional.isPresent()) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.NOT_ACCEPTABLE);
        }

        Optional<Request> optionalRequest = requestRepository.findByFromAndTo(userId, id);
        if (optionalRequest.isPresent()) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.FORBIDDEN);
        }

        Request request = new Request();
        request.setFrom(userId);
        request.setTo(id);
        request.setTime(new Date().getTime());
        requestRepository.save(request);

        Notification notification = new Notification();
        notification.setCategory(Parameter.Category.FRIEND_REQUEST.label);
        notification.setNotification((Pair.of(Parameter.REQUEST, userId)));
        notification.setOwner(id);
        notification.setRead(false);
        notification.setTime(new Date().getTime());
        notificationRepository.save(notification);

        User user;
        if ((user = userRepository.findById(id).get()).getActivity() == Parameter.ONLINE) {
            messageTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_NOTIFY).appendField(Parameter.DATA, true));
        }

        return new ResponseEntity<>(StatusHandler.S200, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("friend")
    public ResponseEntity<JSONObject> deleteRequest(HttpServletRequest req, @RequestParam String id) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        requestRepository.deleteByFromAndTo(userId, id);
        notificationRepository.onAcceptFriend(userId, id);
        return new ResponseEntity<>(StatusHandler.S200, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/friends")
    public ResponseEntity<JSONObject> removeFriend(HttpServletRequest req, @RequestParam String id) {
        String userId = req.getAttribute(Parameter.CLIENT_ID).toString();
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.BAD_REQUEST);
        }
        friendRepository.deleteFriend(userId + ":" + id, id + ":" + userId);
        notificationRepository.onDeleteFriend(userId, id);
        messageRepository.deleteByFromAndTo(userId, id);
        if (user.get().getActivity() == Parameter.ONLINE) {
            messageTemplate.convertAndSendToUser(user.get().getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_DELETE_USER).appendField(Parameter.WS_DATA, userId));
        }
        return new ResponseEntity<>(StatusHandler.S200, HttpStatus.ACCEPTED);
    }

    @PostMapping("/friends")
    public ResponseEntity<JSONObject> getFriends(HttpServletRequest req,
            @RequestParam(required = false, defaultValue = "all") String active) {
        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        List<User> users;
        users = switch (active) {
            case "false" ->
                userRepository.findOfflineFriends(req.getAttribute(Parameter.CLIENT_ID).toString());
            case "true" ->
                userRepository.findOnlineFriends(req.getAttribute(Parameter.CLIENT_ID).toString());
            default ->
                userRepository.findFriends(req.getAttribute(Parameter.CLIENT_ID).toString());
        };
        result.appendField(Parameter.RESULTS, users.toArray());
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PostMapping("/messages")
    public ResponseEntity<JSONObject> getMessages(HttpServletRequest req) {
        JSONObject result = (JSONObject) StatusHandler.S200.clone();
        result.appendField(Parameter.RESULTS, messageRepository.findMessages(req.getAttribute(Parameter.CLIENT_ID).toString()));
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/messages")
    public ResponseEntity<JSONObject> deleteMessages(HttpServletRequest req, @RequestParam String id) {
        String clientId = req.getAttribute(Parameter.CLIENT_ID).toString();
        Optional<Friend> optional = friendRepository.findByFriend(clientId + ":" + id, id + ":" + clientId);
        if (optional.isPresent()) {
            messageRepository.deleteByFromAndTo(id, clientId);
        }
        return new ResponseEntity<>(StatusHandler.S200, HttpStatus.ACCEPTED);
    }

    @PostMapping("/discover")
    public ResponseEntity<JSONObject> discoverPeople(HttpServletRequest req,
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "15") int limit) {
        if (page < 0) {
            page = 0;
        } else {
            page--;
        }
        if (limit < 15) {
            limit = 15;
        }
        String id = req.getAttribute(Parameter.CLIENT_ID).toString();

        if (query.length() < 3) {
            return new ResponseEntity<>(StatusHandler.S200, HttpStatus.BAD_REQUEST);
        }

        Slice<User> users = userRepository.queryPeople(id, query, Pageable.ofSize(limit).withPage(page));
        JSONObject result = (JSONObject) StatusHandler.S200.clone();

        result.put(Parameter.ELEMENTS, users.getNumberOfElements());
        result.put(Parameter.PAGES, users.getNumberOfElements() / limit + (users.getNumberOfElements() % limit > 0 ? 1 : 0));
        result.put(Parameter.RESULTS, users.getContent().toArray());

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }
}

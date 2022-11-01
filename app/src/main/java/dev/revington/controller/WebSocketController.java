package dev.revington.controller;

import dev.revington.entity.Message;
import dev.revington.entity.User;
import dev.revington.repository.MessageRepository;
import dev.revington.repository.UserRepository;
import dev.revington.variables.Parameter;
import dev.revington.variables.StatusHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 *
 * @author micha
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/message")
    public JSONObject sendMessage(@Payload JSONObject msg,
            @Header(Parameter.SIMP_SESSION_ATTRIBUTES) Map<String, Object> attributes,
            @Header(Parameter.SIMP_SESSION_ID) String session) {
        String id = attributes.get(Parameter.CLIENT_ID).toString();
        if (msg.get(Parameter.WS_ACTION).toString().equals(Parameter.WS_ACTION_MESSAGE_SEND) && msg.get(Parameter.WS_CONTENT) != null) {
            HashMap<String, Object> content = (HashMap) msg.get(Parameter.CONTENT);
            String sendTo = content.get(Parameter.WS_ID).toString();
            User user = userRepository.findFriend(id, sendTo);
            if (user == null) {
                return StatusHandler.S200;
            }

            if (messageRepository.countByFromAndTo(id, sendTo) >= 99) {
                return StatusHandler.E1020;
            }

            Message message = new Message();
            message.setFrom(id);
            message.setTo(sendTo);
            message.setContent(content.get(Parameter.CONTENT).toString());
            message.setTime(new Date().getTime());
            if (user.getActive() == null || !user.getActive().equals(id)) {
                messageRepository.save(message);
            }

            if (user.getActivity() == Parameter.ONLINE) {
                messagingTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_MESSAGE_RECIEVE).appendField(Parameter.WS_DATA,
                        new JSONObject().appendField(Parameter.WS_ID, id).appendField(Parameter.WS_CONTENT, message.getContent()).appendField(Parameter.WS_SENT, false)
                ));
                messagingTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_RECIEVE).appendField(Parameter.WS_DATA, id));
            }

            return ((JSONObject) StatusHandler.S200.clone()) /*.appendField(Parameter.WS_MESSAGE_ID, msg.get(Parameter.WS_MESSAGE_ID).toString()) */;
        }

        return StatusHandler.S200;
    }

    /*
    @MessageMapping("/notify")
    public void notify(@Payload JSONObject msg,
            @Header(Parameter.SIMP_SESSION_ATTRIBUTES) Map<String, Object> attributes) {
        String id = attributes.get(Parameter.CLIENT_ID).toString();
        if (!msg.get(Parameter.ACTION).equals(Parameter.WS_ACTION_ACTIVE) && msg.get(Parameter.WS_CONTENT) != null) {
            Optional<User> optionaleUser = userRepository.findById(msg.getAsString(Parameter.CONTENT));
            User user;
            if (optionaleUser.isEmpty() || (user = optionaleUser.get()).getActivity() != Parameter.ONLINE) {
                return;
            }
            
            JSONObject content = (JSONObject) msg.get(Parameter.WS_CONTENT);
            content.put(Parameter.WS_ID, id);
            
            messagingTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION).appendField(Parameter.DATA, content));
        }
    }
     */
    @MessageMapping("/open")
    public void open(@Payload JSONObject msg,
            @Header(Parameter.SIMP_SESSION_ATTRIBUTES) Map<String, Object> attributes) {
        if (msg.getAsString(Parameter.ACTION).equals(Parameter.WS_ACTION_ACTIVE) && msg.get(Parameter.WS_CONTENT) != null) {
            String id = attributes.get(Parameter.CLIENT_ID).toString();
            String client = msg.getAsString(Parameter.WS_CONTENT);
            User friend = userRepository.findFriend(id, client);
            if (friend == null) {
                return;
            }

            User user = userRepository.findById(id).get();
            user.setActive(client);
            userRepository.save(user);
        }
    }

    @MessageMapping("/typing")
    public void notifyTyping(@Payload JSONObject msg,
            @Header(Parameter.SIMP_SESSION_ATTRIBUTES) Map<String, Object> attributes) {
        if (msg.getAsString(Parameter.ACTION).equals(Parameter.WS_ACTION_ACTIVE) && msg.get(Parameter.WS_CONTENT) != null) {
            String id = attributes.get(Parameter.CLIENT_ID).toString();
            HashMap<String, Object> content = (HashMap) msg.get(Parameter.CONTENT);
            Optional<User> optionalUser = userRepository.findById(content.get(Parameter.WS_ID).toString());
            User user;
            if (optionalUser.isEmpty() || (user = optionalUser.get()).getActivity() != Parameter.ONLINE) {
                return;
            }

            content.put(Parameter.WS_ID, id);
            messagingTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.CLIENT_TYPING).appendField(Parameter.DATA, content));
        }
    }

}

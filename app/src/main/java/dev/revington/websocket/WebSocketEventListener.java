package dev.revington.websocket;

import dev.revington.entity.User;
import dev.revington.repository.NotificationRepository;
import dev.revington.repository.UserRepository;
import dev.revington.variables.Parameter;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Date;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        User user = userRepository.findById(SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).get();

        user.setActivity(Parameter.ONLINE);
        user.setSocketId(SimpAttributesContextHolder.currentAttributes().getSessionId());
        user.setTime(0);
        user.setActive("");
        userRepository.save(user); 

        userRepository.findOnlineFriendsWithSocketId(SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).forEach((friend) -> {
            messagingTemplate.convertAndSendToUser(friend.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.ACTION, Parameter.CLIENT_ONLINE).appendField(Parameter.DATA, SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()));
        }); 
    }

    @EventListener
    private void handleSessionSubscription(SessionSubscribeEvent event) {
        User user = userRepository.findById(SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).get();

        if (!notificationRepository.findUnread(user.getId()).isEmpty()){ 
            messagingTemplate.convertAndSendToUser(user.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.WS_ACTION, Parameter.WS_ACTION_NOTIFY).appendField(Parameter.DATA, true));
        }   
    }
    
    @EventListener
    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        User user = userRepository.findById(SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).get();

        user.setActivity(Parameter.OFFLINE);
        user.setSocketId("");
        user.setTime(new Date().getTime());
        user.setActive("");
        userRepository.save(user);

        userRepository.findOnlineFriendsWithSocketId(SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).forEach((friend) -> {
            messagingTemplate.convertAndSendToUser(friend.getSocketId(), Parameter.WS_QUEUE, new JSONObject().appendField(Parameter.ACTION, Parameter.CLIENT_OFFLINE).appendField(Parameter.DATA, 
                        new JSONObject().appendField(Parameter.WS_ID, SimpAttributesContextHolder.getAttributes().getAttribute(Parameter.CLIENT_ID).toString()).appendField(Parameter.WS_TIME, user.getTime())
                    ));
        });
    }

}

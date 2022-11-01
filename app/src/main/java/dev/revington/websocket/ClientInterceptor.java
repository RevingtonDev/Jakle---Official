/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.revington.websocket;

import dev.revington.variables.Parameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 *
 * @author micha
 */
public class ClientInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor;
        if ((accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class)) != null
                && accessor.getUser() == null) {
            accessor.setUser(new Client(message.getHeaders().get(Parameter.SIMP_SESSION_ID).toString()));
        }
        return ChannelInterceptor.super.preSend(message, channel);
    }

}

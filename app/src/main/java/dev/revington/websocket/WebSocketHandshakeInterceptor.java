package dev.revington.websocket;

import dev.revington.variables.Parameter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.*;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {  
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> cookies;
        if ((cookies = request.getHeaders().get(Parameter.COOKIE)) == null || cookies.size() < 1)
            return false;
         
        attributes.put(Parameter.CLIENT_ID, ((ServletServerHttpRequest) request).getServletRequest().getAttribute(Parameter.CLIENT_ID));
        // attributes.put(Parameter.AUTH, new String(Base64.getDecoder().decode(Arrays.stream(cookies.get(0).split(" ")).filter(val -> val.startsWith(Parameter.AUTH )).findFirst().get().replace(Parameter.AUTH + "=", "").replace(";",""))));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        
    } 
    
}

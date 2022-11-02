package dev.revington.api;

import dev.revington.variables.StatusHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL; 
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author michael
 */
@RestController
public class GMailAuthProvider {
    
    private final Logger logger = Logger.getLogger(GMailAuthProvider.class.getName());
    
    @GetMapping("/auth")
    public JSONObject reroute(HttpServletRequest req, @RequestParam String code, @RequestParam String scope) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format("http://localhost:9000/Callback?code=%s&scode=%s", code, scope)).openConnection();
            logger.log(Level.WARNING, new String(connection.getInputStream().readAllBytes()));
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        return StatusHandler.S200;
    }
    
}

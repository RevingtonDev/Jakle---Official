package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.revington.util.DefaultValueFilter;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author micha
 */
@Document("messages")
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class Message {
    
    private String id;
    
    private String from;
    
    private String to;
    
    private String content;
    
    private long time;
    
    private boolean send;

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    } 

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
    
}

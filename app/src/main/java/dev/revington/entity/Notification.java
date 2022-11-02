package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude; 
import dev.revington.util.DefaultValueFilter; 
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;
 
import java.util.HashMap;
 
@Document("notifications")
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class Notification {

    @Id
    private String id;

    private String owner;
    private String category;
    private Pair<String, String> notification;

    private boolean read;

    private long time;

    private HashMap<String, Object> detail;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public Pair<String, String> getNotification() {
        return notification;
    }

    public String getOwner() {
        return owner;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setNotification(Pair<String, String> notification) {
        this.notification = notification;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public HashMap<String, Object> getDetail() {
        return detail;
    }

    public void setDetail(HashMap<String, Object> detail) {
        this.detail = detail;
    }
}

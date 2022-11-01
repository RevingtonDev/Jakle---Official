package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.revington.util.DefaultValueFilter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("friends")
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class Friend {

    private String id;

    private String friends;

    private long timestamp;

    private String first;
    private String second;

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}

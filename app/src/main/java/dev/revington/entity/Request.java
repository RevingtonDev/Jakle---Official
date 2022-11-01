package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.revington.util.DefaultValueFilter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("requests")
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class Request {

    private String id;

    private String from;
    private String to;

    public long time;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

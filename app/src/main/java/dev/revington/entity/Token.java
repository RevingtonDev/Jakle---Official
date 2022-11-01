package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.revington.util.DefaultValueFilter;

@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class Token {

    private String id;

    private String clientId;
    private String token;
    
    private long expires;
    private long created;

    private int grants;

    public String getClientId() {
        return clientId;
    }

    public long getExpires() {
        return expires;
    }

    public String getToken() {
        return token;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getGrants() {
        return grants;
    }

    public void setGrants(int grants) {
        this.grants = grants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
    
}

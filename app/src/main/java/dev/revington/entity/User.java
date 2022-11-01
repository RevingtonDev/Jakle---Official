package dev.revington.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.revington.util.DefaultValueFilter;
import java.util.ArrayList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import net.minidev.json.JSONObject;

@Document("users")
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DefaultValueFilter.class)
public class User {

    private String firstname; 
    private String lastname;
    
    @Id
    private String id;
    private String name; 
    private String email;
    private String password;
    private String profilePic;
    private String socketId;
    private String active;
    private String type;

    private Date dateOfBirth;

    private int attempts;
    private int status;
    private int validity;
    private int activity;
    private int count; 
 
    private long time;
    private long created;

    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
        return created;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getActivity() {
        return activity;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    } 

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    } 

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package dev.revington.websocket;

import java.security.Principal;
import javax.security.auth.Subject;
 
public class Client implements Principal {

    private final String name;

    public Client(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.revington.websocket;

import java.security.Principal;
import javax.security.auth.Subject;

/**
 *
 * @author micha
 */
public class Client implements Principal {

    private String name;

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

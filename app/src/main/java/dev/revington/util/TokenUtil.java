package dev.revington.util;

import dev.revington.entity.Token;
import dev.revington.entity.User;
import dev.revington.variables.Parameter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

public class TokenUtil {

    public static Token generateToken(User user, int grant, long timeout) throws NoSuchAlgorithmException {
        Token token = new Token();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        token.setExpires(timestamp.getTime() + timeout);
        token.setCreated(timestamp.getTime());
        token.setToken(new String(Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest((user.getEmail() + " : " + user.getName() + " " + timestamp.toString()).getBytes(StandardCharsets.UTF_8)))));
        token.setGrants(grant);
        token.setClientId(user.getId());
        return token;
    }

    public static Token regenToken(User user, Token token, long timeout) throws NoSuchAlgorithmException {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        token.setToken(new String(Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest((user.getEmail() + " : " + user.getName() + " " + timestamp.toString()).getBytes(StandardCharsets.UTF_8)))));
        token.setCreated(timestamp.getTime());
        token.setExpires(timestamp.getTime() + timeout);
        return token;
    }

}

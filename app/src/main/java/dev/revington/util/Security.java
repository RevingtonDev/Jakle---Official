/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.revington.util;

import dev.revington.controller.Credential;
import dev.revington.entity.Token;
import dev.revington.entity.User;
import dev.revington.mail.JMail;
import dev.revington.variables.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author micha
 */
public class Security {

    private static final Logger logger = Logger.getLogger(Security.class.getName());

    public static String md5Hash(String plain) {
        return DigestUtils.md5DigestAsHex(plain.getBytes());
    }

    public static String normalizeEmail(String email) {
        email = email.toLowerCase();
        int at = email.indexOf('@');
        return email.substring(0, at).replace(".", "").replace("+", "") + email.substring(at);
    }

    public static boolean sendActivation(JMail mailService, User user, Token token, String name, String url) {
        try (InputStream mailStream = Security.class.getResourceAsStream(token.getGrants() == Parameter.GRANT_REACTIVATION ? "/mail/reactivation.mail" : "/mail/activation.mail")) {
            if (mailStream == null) {
                throw new FileNotFoundException();
            }
            logger.log(Level.SEVERE, mailStream == null ? "null" : " askdla");
            String content = String.format(new String(mailStream.readAllBytes()), name, url + (token.getGrants() == Parameter.GRANT_REACTIVATION ? "/reactivate" : "/activate"), Base64.getEncoder().encodeToString(token.getToken().getBytes()));
            return mailService.sendMail(user.getEmail(), (token.getGrants() == Parameter.GRANT_REACTIVATION ? "Account Reactivation" : "Account Verification"), content);
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            return false;
        } 
    }

    public static boolean sendReset(JMail mailService, User user, Token token, String name, String url) {
        try ( InputStream mailStream = Security.class.getResourceAsStream("/mail/reset.mail")) {
            if (mailStream == null) {
                throw new FileNotFoundException();
            }

            String content = String.format(new String(mailStream.readAllBytes()), name, url + "/reset", Base64.getEncoder().encodeToString(token.getToken().getBytes()));
            if (!mailService.sendMail(user.getEmail(), "Password Reset", content)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            return false;
        }
    }
}

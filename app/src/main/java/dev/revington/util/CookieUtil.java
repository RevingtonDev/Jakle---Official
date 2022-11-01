package dev.revington.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class CookieUtil {

    public static String getValue(HttpServletRequest req, String name) {
        if(req.getCookies() == null) return "";

        Optional<Cookie> cookie = Arrays.stream(req.getCookies()).filter(c -> c.getName().equals(name)).findAny();
        if(cookie.isPresent()) return cookie.get().getValue();
        else return "";
    }

    public static void addCookie(HttpServletResponse response, String name, Consumer<Cookie> func) {
        Cookie cookie = new Cookie(name, "");
        func.accept(cookie);
        response.addCookie(cookie);
    }

}

package dev.revington.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Client {

    @GetMapping(value = { "/", "/op", "/op/**" })
    public String index(HttpServletRequest req, HttpServletResponse res) { 
        return "index";
    }

}

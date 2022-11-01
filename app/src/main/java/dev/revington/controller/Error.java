package dev.revington.controller;

import dev.revington.variables.Parameter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Error implements ErrorController {

    @Value("${spring.react.css}")
    private String css;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("css", css);
        int status = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        model.addAttribute(Parameter.CODE, status);
        switch (status) {
            case 401 -> model.addAttribute(Parameter.QUERY, "You can't be here.");
            case 403 -> model.addAttribute(Parameter.QUERY, "You're not supposed to be here.");
            case 404 -> model.addAttribute(Parameter.QUERY, "Page Not Found.");
            case 500 -> model.addAttribute(Parameter.QUERY, "Internal Server Error.");
            default -> model.addAttribute(Parameter.QUERY, "Something went wrong.");
        }
        return "error";
    }

}

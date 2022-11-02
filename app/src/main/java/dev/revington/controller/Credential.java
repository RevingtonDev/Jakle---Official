package dev.revington.controller;
 
import dev.revington.entity.Token;
import dev.revington.entity.User;
import dev.revington.mail.JMail;
import dev.revington.repository.TokenRepository;
import dev.revington.util.CookieUtil;
import dev.revington.util.TokenUtil;
import dev.revington.variables.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.*;

import dev.revington.repository.UserRepository;
import dev.revington.util.Security; 
import jakarta.servlet.http.HttpServletRequest;
 
import java.io.IOException; 
import java.nio.charset.StandardCharsets; 
import java.security.NoSuchAlgorithmException; 
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger; 

@Controller
public class Credential {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JMail mailService;

    @Value("${spring.react.css}")
    private String css;

    private Logger logger = Logger.getLogger(Credential.class.getName());

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest req) {
        model.addAttribute("link", "Login");
        model.addAttribute("css", css);

        String message;
        if ((message = (String) req.getSession().getAttribute(Parameter.ACTION)) != null) {
            model.addAttribute("message", true);
            model.addAttribute(Parameter.ACTION, message);
            req.getSession().removeAttribute(Parameter.ACTION);
        }

        String error;
        if ((error = (String) req.getSession().getAttribute(Parameter.DESCRIPTION)) != null) {
            model.addAttribute("error", true);
            model.addAttribute("des", error);
            req.getSession().removeAttribute(Parameter.DESCRIPTION);
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest req, HttpServletResponse resp, @ModelAttribute User user, @RequestParam(required = false, defaultValue = "false") String remember,
            Model model, HttpServletResponse response) throws NoSuchAlgorithmException, IOException {
        user.setEmail(Security.normalizeEmail(user.getEmail()));
        Optional<User> optional = userRepository.findByEmail(user.getEmail());
        if (optional.isPresent()) {
            User client = optional.get();
            if (client.getPassword().equals(Security.md5Hash(user.getPassword()))) {
                client.setAttempts(0);
                userRepository.save(client);
                if (client.getStatus() == Parameter.DISABLED || client.getValidity() != Parameter.ACTIVATED) {
                    model.addAttribute("activate", true);
                    model.addAttribute("error", true);
                    model.addAttribute("des", client.getStatus() == Parameter.DISABLED ? Parameter.ACCOUNT_DISABLED : Parameter.INACTIVE_ACC);
                    req.getSession().setAttribute(Parameter.CLIENT_ID, client.getEmail());
                } else if (client.getValidity() == Parameter.ACTIVATED) {
                    Token token = TokenUtil.generateToken(client, Parameter.ALL_GRANTS, (1000L * Parameter.COOKIE_TIMEOUT));
                    CookieUtil.addCookie(response, Parameter.AUTH, (cookie) -> {
                        cookie.setValue(Base64.getUrlEncoder().encodeToString(token.getToken().getBytes(StandardCharsets.UTF_8)));
                        cookie.setHttpOnly(true);
                        // cookie.setSecure(true);
                        if (remember.equals("true")) {
                            cookie.setMaxAge(Parameter.COOKIE_TIMEOUT);
                        }
                    });
                    tokenRepository.save(token);
                    response.sendRedirect("/op");
                }
            } else {
                model.addAttribute("error", true);
                model.addAttribute("des", Parameter.NO_ACCOUNT);

                if (client.getStatus() == Parameter.ACTIVE) {
                    client.setAttempts(client.getAttempts() + 1);

                    if (client.getAttempts() >= 8) {
                        client.setStatus(Parameter.DISABLED);
                        client.setAttempts(0);
                        tokenRepository.deleteAllByClientId(client.getId());

                        Token token = TokenUtil.generateToken(user, Parameter.GRANT_REACTIVATION, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
                        if (!Security.sendActivation(mailService, client, token, client.getName().split(" ")[0], req.getRequestURL().toString().replace(req.getRequestURI(), ""))) {
                            resp.sendError(500);
                        } else {
                            tokenRepository.save(token);
                        }
                    }

                    userRepository.save(client);
                }
            }
        } else {
            model.addAttribute("error", true);
            model.addAttribute("des", Parameter.NO_ACCOUNT);
        }
        model.addAttribute("css", css);
        return "login";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute("css", css);
        model.addAttribute("error", false);
        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(Model model, @ModelAttribute User user, HttpServletResponse resp, HttpServletRequest req) throws IOException {
        user.setEmail(Security.normalizeEmail(user.getEmail()));
        Optional<User> imp = userRepository.findByEmail(user.getEmail());
        if (imp.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("des", Parameter.DUP_EMAIL);
        } else {
            String name = user.getFirstname();
            user.setName(user.getFirstname() + " " + user.getLastname());
            user.setFirstname(null);
            user.setLastname(null);
            user.setAttempts(0);
            user.setValidity(0);
            user.setCreated(new Date().getTime());
            user.setPassword(Security.md5Hash(user.getPassword()));
            userRepository.save(user);
            user = userRepository.findByEmail(user.getEmail()).get();

            Token token = null;
            try {
                token = TokenUtil.generateToken(user, Parameter.GRANT_ACTIVATION, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            tokenRepository.save(token);
            if (!Security.sendActivation(mailService, user, token, name, req.getRequestURL().toString().replace(req.getRequestURI(), ""))) {
                resp.sendError(500);
            } else {
                req.getSession().setAttribute(Parameter.ACTION, Parameter.NEW_ACCOUNT_CREATED);
                resp.sendRedirect("/login");
            }
        }
        model.addAttribute("css", css);
        return "signup";
    }

    @PostMapping("/activate")
    public void activate(HttpServletRequest req, HttpServletResponse resp, @RequestParam String token) throws IOException {
        token = new String(Base64.getDecoder().decode(token.getBytes()));
        Token tokenObj;
        if ((tokenObj = tokenRepository.findByToken(token)) != null && tokenObj.getGrants() == Parameter.GRANT_ACTIVATION) {
            tokenRepository.delete(tokenObj);
            if (new Date().getTime() > tokenObj.getExpires()) {
                req.getSession().setAttribute(Parameter.ACTION, Parameter.EXPIRED_TOKEN);
            } else {
                User user = userRepository.findById(tokenObj.getClientId()).get();
                user.setStatus(Parameter.ACTIVE);
                userRepository.save(user);

                req.getSession().setAttribute(Parameter.ACTION, "Account successfully reactivated.");
            }
        } else {
            req.getSession().setAttribute(Parameter.ACTION, Parameter.WENT_WRONG);
        }

        resp.sendRedirect("/login");
    }

    @GetMapping("/activate")
    public void sendActivation(HttpServletRequest req, HttpServletResponse resp, @RequestParam(required = false, defaultValue = "") String email) throws IOException {
        Token token = null;
        User user = null;
        Object emailObj = req.getSession().getAttribute(Parameter.CLIENT_ID);

        if (email.equals("")) {
            if (emailObj == null) {
                req.getSession().setAttribute("des", Parameter.WENT_WRONG);
                resp.sendRedirect("./login");
                return;
            }

            email = emailObj.toString();
            req.getSession().removeAttribute(Parameter.CLIENT_ID);
        }

        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) {
            logger.log(Level.SEVERE, "No account.");
            req.getSession().setAttribute("des", Parameter.WENT_WRONG);
            resp.sendRedirect("./login");
            return;
        } else {
            user = optional.get();
        }

        long now = new Date().getTime();
        try {
            if (user.getValidity() == Parameter.ACTIVATED) {
                token = tokenRepository.findTokenByEmail(email, Parameter.GRANT_REACTIVATION);
                if (token == null) {
                    token = TokenUtil.generateToken(user, Parameter.GRANT_REACTIVATION, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
                } else {
                    if (token.getCreated() + (1000L * 60 * 15) > now) {
                        logger.log(Level.SEVERE, "Too soon.");
                        req.getSession().setAttribute(Parameter.DESCRIPTION, Parameter.TRY_AGAIN);
                        resp.sendRedirect("./login");
                        return;
                    }
                    token = TokenUtil.regenToken(user, token, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
                }
            } else {
                token = tokenRepository.findTokenByEmail(email, Parameter.GRANT_ACTIVATION);
                if (token == null) {
                    token = TokenUtil.generateToken(user, Parameter.GRANT_ACTIVATION, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
                } else {
                    if (token.getCreated() + (1000L * 60 * 15) > now) {
                        logger.log(Level.SEVERE, "Too soon.");
                        req.getSession().setAttribute(Parameter.DESCRIPTION, Parameter.TRY_AGAIN);
                        resp.sendRedirect("./login");
                        return;
                    }
                    token = TokenUtil.regenToken(user, token, (1000L * Parameter.ACTIVATION_TOKEN_TIMEOUT));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, null, e);
            req.getSession().setAttribute(Parameter.DESCRIPTION, Parameter.WENT_WRONG);
            resp.sendRedirect("./login");
            return;
        }

        tokenRepository.save(token);
        if (Security.sendActivation(mailService, user, token, (user.getName() == null ? "User" : user.getName().split(" ")[0]), req.getRequestURL().toString().replace(req.getRequestURI(), ""))) {
            req.getSession().setAttribute(Parameter.ACTION, Parameter.LINK_SENT_SUCCESSFUL);
        } else {
            logger.log(Level.SEVERE, "Email not send.");
            req.getSession().setAttribute(Parameter.DESCRIPTION, Parameter.WENT_WRONG);
        }

        resp.sendRedirect("./login");
    }

    @PostMapping("/reactivate")
    public void reactivateAccount(HttpServletRequest req, HttpServletResponse resp, @RequestParam String token) throws IOException {
        token = new String(Base64.getDecoder().decode(token.getBytes()));
        Token tokenObj = tokenRepository.findByToken(token);
        if (tokenObj != null) {
            tokenRepository.delete(tokenObj);
            if (new Date().getTime() > tokenObj.getExpires()) {
                req.getSession().setAttribute(Parameter.ACTION, Parameter.EXPIRED_TOKEN);
            } else {
                User user = userRepository.findById(tokenObj.getClientId()).get();
                user.setValidity(Parameter.ACTIVATED);
                user.setStatus(Parameter.ACTIVE);
                userRepository.save(user);

                req.getSession().setAttribute(Parameter.ACTION, "Account successfully activated.");
            }
        } else {
            logger.log(Level.SEVERE, "No token found.");
            req.getSession().setAttribute(Parameter.ACTION, Parameter.WENT_WRONG);
        }

        resp.sendRedirect("/login");
    }

    @GetMapping("/reset")
    public String getReset(Model model, HttpServletRequest req, HttpServletResponse resp, @RequestParam String token) throws IOException {
        Token tokenObj = tokenRepository.findByToken(new String(Base64.getUrlDecoder().decode(token)));

        if (tokenObj == null || tokenObj.getGrants() != Parameter.GRANT_RESET) {
            req.getSession().setAttribute(Parameter.ACTION, Parameter.WENT_WRONG);
            resp.sendRedirect("/login");
            return "login";
        }

        tokenRepository.delete(tokenObj);
        if (tokenObj.getExpires() < new Date().getTime()) {
            req.getSession().setAttribute(Parameter.ACTION, Parameter.EXPIRED_TOKEN);
            resp.sendRedirect("/login");
            return "login";
        }

        User user = userRepository.findById(tokenObj.getClientId()).get();
        req.getSession().setAttribute(Parameter.CLIENT_ID, user.getId());
        req.getSession().setAttribute("reset", true);
        model.addAttribute("css", css);
        return "reset";
    }

    @PostMapping("/reset")
    public String resetPassword(HttpServletRequest req, HttpServletResponse resp, @RequestParam String password) throws IOException {
        Object userId;
        Optional<User> optional = null;
        if ((userId = req.getSession().getAttribute(Parameter.CLIENT_ID)) == null || ((optional = userRepository.findById(userId.toString()))).isEmpty()) {
            req.getSession().setAttribute(Parameter.ACTION, Parameter.WENT_WRONG);
            resp.sendRedirect("/login");
        }
        req.getSession().removeAttribute(Parameter.CLIENT_ID);

        User user = optional.get();
        user.setPassword(Security.md5Hash(password));
        userRepository.save(user);

        tokenRepository.deleteAllByClientId(user.getId());
        req.getSession().setAttribute(Parameter.ACTION, "Successfully changed password.");
        resp.sendRedirect("/login");
        return "reset";
    }

}

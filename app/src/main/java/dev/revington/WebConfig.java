package dev.revington;

import dev.revington.entity.Token;
import dev.revington.repository.TokenRepository;
import dev.revington.util.CookieUtil;
import dev.revington.variables.Parameter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Configuration
@EnableWebSecurity
public class WebConfig {

    @Autowired
    TokenRepository tokenRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "auth", "/login", "/signup", "/reset").permitAll();

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<GuestFilter> guestFilter() {
        FilterRegistrationBean<GuestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GuestFilter());
        registrationBean.addUrlPatterns("/", "/auth", "/login", "/activate", "/reactivate", "/signup", "/api/v1/activate");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthorizeFilter> authorizeFilter() {
        FilterRegistrationBean<AuthorizeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthorizeFilter());
        registrationBean.addUrlPatterns("/op/*", "/api/*");
        return registrationBean;
    }

    protected class GuestFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String auth = new String(Base64.getUrlDecoder().decode(CookieUtil.getValue(request, Parameter.AUTH)));
            if (!auth.equals("")) {
                Token token = tokenRepository.findByToken(auth);

                if (token != null) {
                    if (token.getExpires() > new Date().getTime()) {
                        response.sendRedirect("/op");
                    } else {
                        CookieUtil.addCookie(response, Parameter.AUTH, (cookie -> {
                            cookie.setMaxAge(0);
                        }));
                        tokenRepository.deleteByToken(auth);
                        filterChain.doFilter(request, response);
                    }
                } else {
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }

    }

    protected class AuthorizeFilter extends OncePerRequestFilter {

        final List<String> ignore = Arrays.asList("/api/v1/reset-link");

        @Override
        protected boolean shouldNotFilter(HttpServletRequest req) throws ServletException {
            return ignore.stream().anyMatch(val -> val.equals(req.getRequestURI()));
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String auth = new String(Base64.getUrlDecoder().decode(CookieUtil.getValue(request, Parameter.AUTH)));
            if (!auth.equals("")) {
                Token token = tokenRepository.findByToken(auth);

                if (token != null) {
                    if (token.getExpires() > new Date().getTime()) {
                        request.setAttribute(Parameter.CLIENT_ID, token.getClientId());
                        request.setAttribute(Parameter.AUTH, auth);
                        filterChain.doFilter(request, response);
                    } else {
                        CookieUtil.addCookie(response, Parameter.AUTH, (cookie -> {
                            cookie.setMaxAge(0);
                        }));
                        tokenRepository.deleteByToken(auth);
                        response.sendError(401);
                    }
                } else {
                    response.sendError(401);
                }
            } else {
                response.sendError(404);
            }
        }

    }

}

package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@Controller
@ResponseBody
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @GetMapping("/hi")
    Map<String, String> home(Principal principal) {
        return Map.of("message", "hello " + principal.getName());
    }

    @Bean
    SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(Customizer.withDefaults())
                .authorizeHttpRequests(ae -> ae.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .oneTimeTokenLogin(config -> config.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {

                    System.out.println("click on this link: http://localhost:8080/login/ott?token=" +
                            oneTimeToken.getTokenValue());

                    // tbd twillio or sendgrid
                    response.getWriter().print("you've got console mail!");
                    response.setContentType(MediaType.TEXT_PLAIN.toString());


                }))
                .webAuthn(wa -> wa
                        .rpName("Bootiful")
                        .rpId("localhost")
                        .allowedOrigins("http://localhost:8080", "http://localhost:8081",
                                "http://127.0.0.1:8080", "http://127.0.0.1:8081"))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pe) {
        var users = Set.of(
                User.withUsername("jlong")
                        .password(pe.encode("pw"))
                        .roles("USER")
                        .build(),
                User.withUsername("jblum")
                        .password(pe.encode("pw"))
                        .roles("USER", "ADMIN")
                        .build()
        );
        return new InMemoryUserDetailsManager(users);
    }
}

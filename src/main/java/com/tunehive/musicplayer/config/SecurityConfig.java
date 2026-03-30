package com.tunehive.musicplayer.config;

import com.tunehive.musicplayer.model.User;
import com.tunehive.musicplayer.storage.TempStorage;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // ← Let controllers handle auth themselves
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                    String email  = oauthUser.getAttribute("email");
                    String name   = oauthUser.getAttribute("name");
                    if (email == null) email = "";
                    if (name  == null) name  = "Google User";
                    String mobile = "G" + Math.abs((long) email.hashCode() % 1_000_000_000L);
                    if (!TempStorage.users.containsKey(mobile)) {
                        User user = new User();
                        user.setMobile(mobile);
                        user.setEmail(email);
                        user.setPremium(false);
                        user.setPlan("FREE");
                        TempStorage.users.put(mobile, user);
                    }
                    HttpSession session = request.getSession(true);
                    session.setAttribute("mobile",  mobile);
                    session.setAttribute("name",    name);
                    session.setAttribute("email",   email);
                    session.setAttribute("premium", false);
                    session.setAttribute("plan",    "FREE");
                    response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/login?error=oauth_failed");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/custom-logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
package com.tunehive.musicplayer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/signup", "/login", "/send-otp",
                                 "/verify-otp", "/do-login", "/css/**",
                                 "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")        // ← your custom login page
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")        // ← same custom login page
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}


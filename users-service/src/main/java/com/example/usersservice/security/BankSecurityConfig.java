package com.example.usersservice.security;

import com.example.usersservice.jwt.JwtFilter;
import com.example.usersservice.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@Configuration
public class BankSecurityConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    public BankSecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        return http
                .httpBasic(httpSec -> httpSec.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/login", "/auth/token")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET,"/users").hasAuthority(Role.ADMIN.name())
                                .anyRequest()
                                .authenticated())
                .addFilterAfter(jwtFilter,  UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}

package com.example.usersservice.security;

import com.example.usersservice.jwt.JwtFilter;
import com.example.usersservice.services.UserService;
import com.example.usersservice.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
public class BankSecurityConfig {

    private final JwtFilter jwtFilter;

    private final UserService userDetails;

    @Autowired
    public BankSecurityConfig(JwtFilter jwtFilter, UserService userDetails) {
        this.jwtFilter = jwtFilter;
        this.userDetails = userDetails;
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
                                .anyRequest()
                                .authenticated())
                .addFilterAfter(jwtFilter,  UsernamePasswordAuthenticationFilter.class)
                .build();
    }
/*
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setPasswordEncoder(passwordEncoder());
        dao.setUserDetailsService((UserServiceImpl) userDetails);
        return dao;
    }

 */
}

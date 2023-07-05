package com.example.usersservice.security;

import com.example.usersservice.services.UserService;
import com.example.usersservice.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
public class BankSecurityConfig {

    private final HandlerExceptionResolver resolver;

    private final UserService userDetails;

    private final JwtTokenRepository jwtTokenRepository;

    @Autowired
    public BankSecurityConfig(UserService userDetails, JwtTokenRepository jwtTokenRepository, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.userDetails = userDetails;
        this.jwtTokenRepository = jwtTokenRepository;
        this.resolver = resolver;
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(antMatcher(HttpMethod.POST, "/users"))
                .permitAll()
                .anyRequest()
                .authenticated()
        ).httpBasic(Customizer.withDefaults())
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
        return http.build();
    }

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
}

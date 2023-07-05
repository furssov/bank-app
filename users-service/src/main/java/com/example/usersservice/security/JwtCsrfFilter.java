package com.example.usersservice.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class JwtCsrfFilter extends OncePerRequestFilter {
    private final JwtTokenRepository repository;

    private final HandlerExceptionResolver resolver;

    public JwtCsrfFilter(JwtTokenRepository repository, HandlerExceptionResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      request.setAttribute(HttpServletResponse.class.getName(), response);
        CsrfToken csrfToken = repository.loadToken(request);
        boolean missingToken = csrfToken == null;
        if (missingToken) {
            csrfToken = repository.generateToken(request);
            repository.saveToken(csrfToken, request, response);
        }
        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        request.setAttribute(csrfToken.getParameterName(), csrfToken);
        if (request.getServletPath().equals("/auth/login")) {
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                resolver.resolveException(request, response, null, new MissingCsrfTokenException(csrfToken.getToken()));
            }
        } else {
            String actualToken = request.getHeader(csrfToken.getHeaderName());
            if (actualToken == null) {
                actualToken = request.getParameter(csrfToken.getParameterName());
            }
            try {
                if (!StringUtils.isEmpty(actualToken)) {
                    Jwts.parser()
                            .setSigningKey(((JwtTokenRepository) repository).getSecret())
                            .parseClaimsJws(actualToken);

                    filterChain.doFilter(request, response);
                } else
                    resolver.resolveException(request, response, null, new InvalidCsrfTokenException(csrfToken, actualToken));
            } catch (JwtException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Invalid CSRF token found for " + UrlUtils.buildFullRequestUrl(request));
                }

                if (missingToken) {
                    resolver.resolveException(request, response, null, new MissingCsrfTokenException(actualToken));
                } else {
                    resolver.resolveException(request, response, null, new InvalidCsrfTokenException(csrfToken, actualToken));
                }
            }
        }
    }
}

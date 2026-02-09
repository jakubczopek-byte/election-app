package pl.election.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static pl.election.config.SecurityHeaders.*;

@Component
public class SecurityHeadersConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader(X_CONTENT_TYPE_OPTIONS, VALUE_NOSNIFF);
        response.setHeader(X_FRAME_OPTIONS, VALUE_DENY);
        response.setHeader(CONTENT_SECURITY_POLICY, VALUE_CSP);
        response.setHeader(STRICT_TRANSPORT_SECURITY, VALUE_HSTS);
        chain.doFilter(request, response);
    }
}

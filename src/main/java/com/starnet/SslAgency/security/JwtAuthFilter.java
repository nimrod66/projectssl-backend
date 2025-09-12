package com.starnet.SslAgency.security;

import com.starnet.SslAgency.processor.repository.StaffRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Parse JWT and extract email
                Claims claims = jwtUtil.parse(token);
                String email = claims.getSubject();

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    staffRepository.findByEmail(email).ifPresent(staff -> {
                        // Attach role to authentication
                        var authority = new SimpleGrantedAuthority("ROLE_" + staff.getRole().name());
                        var authToken = new UsernamePasswordAuthenticationToken(staff, null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                }

            } catch (Exception e) {
                log.warn("JWT invalid or expired: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}

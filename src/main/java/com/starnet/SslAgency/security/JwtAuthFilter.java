package com.starnet.SslAgency.security;

import com.starnet.SslAgency.applicant.repository.ApplicantRepository;
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
    private ApplicantRepository applicantRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Allow preflight OPTIONS requests to pass through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No JWT found in request headers");
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parse(token);
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if ("APPLICANT".equals(role)) {
                    applicantRepository.findByApplicantNumber(subject).ifPresent(applicant -> {
                        log.info("JWT valid for applicant: {}", applicant.getApplicantNumber());
                        var authority = new SimpleGrantedAuthority("ROLE_APPLICANT");
                        var authToken = new UsernamePasswordAuthenticationToken(applicant, null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                } else {
                    staffRepository.findByEmail(subject).ifPresent(staff -> {
                        log.info("JWT valid for user: {}, role: {}", staff.getEmail(), staff.getRole());
                        // Attach role with ROLE_ prefix
                        var authority = new SimpleGrantedAuthority("ROLE_" + staff.getRole().name());
                        var authToken = new UsernamePasswordAuthenticationToken(staff, null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                }
            }

        } catch (Exception e) {
            log.warn("JWT invalid or expired: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}

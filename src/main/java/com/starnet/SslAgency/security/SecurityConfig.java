package com.starnet.SslAgency.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                }) // uses your WebConfig for CORS
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/applications").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/applications/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/applications/public").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/international").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/international/public").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/media/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Authentication
                        .requestMatchers(HttpMethod.GET, "/api/applications/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/applications/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/applications/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/international/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/international/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/international/**").authenticated()
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

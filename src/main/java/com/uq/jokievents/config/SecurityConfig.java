package com.uq.jokievents.config;

import com.uq.jokievents.utils.jwt.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final JwtRequestFilter jwtRequestFilter;

    private final AuthenticationProvider authProvider;

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF as we are handling token-based authentication
//                .authorizeHttpRequests((authRequest) ->
//                        authRequest
//                                .requestMatchers("/auth/**").permitAll() // Allow unauthenticated access to /auth endpoints
//                                .anyRequest().authenticated() // All other endpoints require authentication
//                )
//                .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions for JWT
//                .authenticationProvider(authProvider) // Use custom authentication provider
//                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class) // Add JwtRequestFilter before UsernamePasswordAuthenticationFilter
//                .build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF as we are handling token-based authentication
                .authorizeHttpRequests((authRequest) ->
                        authRequest.anyRequest().permitAll() // Permit all requests from any route while we test this project.
                )
                .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions for JWT
                .build();
    }
}

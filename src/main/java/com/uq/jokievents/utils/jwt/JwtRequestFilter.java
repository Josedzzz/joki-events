package com.uq.jokievents.utils.jwt;

import com.uq.jokievents.service.interfaces.AdminService;
import com.uq.jokievents.service.interfaces.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if(token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        if (
                StringUtils.hasText(authorizationHeader) &&
                        authorizationHeader.startsWith("Bearer ")
        ) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}

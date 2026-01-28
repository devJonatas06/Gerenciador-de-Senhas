package com.project.passwordmanager.PasswordManager.auth.infra.security;

import com.project.passwordmanager.PasswordManager.auth.dto.UserPrincipal;
import com.project.passwordmanager.PasswordManager.auth.entity.User;
import com.project.passwordmanager.PasswordManager.auth.repository.UserRepository;
import com.project.passwordmanager.PasswordManager.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(


            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("SECURITY FILTER EXECUTADO");

        String path = request.getRequestURI();

        if (
                path.startsWith("/auth") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger-ui")
        ) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = recoverToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (token != null) {
            String email = tokenService.validateToken(token);

            if (email != null) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User Not Found"));

                UserPrincipal userPrincipal = UserPrincipal.from(user);

                var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_USER")
                );

                var authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
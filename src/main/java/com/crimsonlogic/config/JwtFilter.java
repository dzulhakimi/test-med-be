package com.crimsonlogic.config;

import com.crimsonlogic.repository.AppUserRepository;
import com.crimsonlogic.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppUserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("🔍 JwtFilter check path: " + path); // for debugging
        return path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.startsWith("/h2-console");
    }


    @Override
    protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, 
                                    javax.servlet.http.HttpServletResponse response, 
                                    javax.servlet.FilterChain filterChain)
            throws javax.servlet.ServletException, java.io.IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<com.crimsonlogic.model.AppUser> user = userRepository.findByUsername(username);
                if (user.isPresent() && jwtService.validateToken(token, username)) {
                    UserDetails userDetails = User.builder()
                        .username(user.get().getUsername())
                        .password(user.get().getPassword())
                        .roles(user.get().getRole())
                        .build();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

package com.jobportal.backend.config;

import com.jobportal.backend.service.CustomUserDetailsService;
import com.jobportal.backend.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Get Authorization header
        final String authorizationHeader = request.getHeader("Authorization");
        
        String email = null;
        String jwt = null;
        
        // 2. Check if header contains Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);  // Remove "Bearer " prefix
            
            try {
                email = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.error("JWT token expired: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error extracting username from JWT: {}", e.getMessage());
            }
        }
        
        // 3. Validate token and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            if (jwtUtil.validateToken(jwt, userDetails)) {
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                        );
                
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                
                log.debug("User authenticated: {}", email);
            }
        }
        
        // 4. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
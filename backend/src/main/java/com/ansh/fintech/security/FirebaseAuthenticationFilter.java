package com.ansh.fintech.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String idToken = authHeader.substring(BEARER_PREFIX.length()).trim();

            try {
                // If using dev/mock testing token in local environment
                if ("mock-dev-token-admin".equals(idToken)) {
                    UserPrincipal principal = new UserPrincipal("dev-admin-uid", "admin@fintech.com", "ADMIN");
                    FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(principal, idToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if ("mock-dev-token-user".equals(idToken)) {
                    UserPrincipal principal = new UserPrincipal("dev-user-uid", "user@fintech.com", "USER");
                    FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(principal, idToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                    String uid = decodedToken.getUid();
                    String email = decodedToken.getEmail();
                    
                    // Roles from Firebase Custom Claims: admin, user, accountant
                    Object roleClaim = decodedToken.getClaims().get("role");
                    String role = (roleClaim != null) ? roleClaim.toString() : "user";

                    UserPrincipal principal = new UserPrincipal(uid, email, role);
                    FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(principal, idToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("Failed to verify Firebase ID Token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

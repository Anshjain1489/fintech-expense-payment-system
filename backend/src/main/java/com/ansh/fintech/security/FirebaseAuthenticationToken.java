package com.ansh.fintech.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;
    private final String credentialsToken;

    public FirebaseAuthenticationToken(UserPrincipal principal, String credentialsToken) {
        super(principal.getAuthorities());
        this.principal = principal;
        this.credentialsToken = credentialsToken;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentialsToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}

package com.ansh.fintech.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class FirebaseAuthenticationFilterTest {

    private FirebaseAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new FirebaseAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterWithMockAdminTokenSetsAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        request.addHeader("Authorization", "Bearer mock-dev-token-admin");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertTrue(principal instanceof UserPrincipal);
        UserPrincipal userPrincipal = (UserPrincipal) principal;

        assertEquals("dev-admin-uid", userPrincipal.getUid());
        assertEquals("ROLE_ADMIN", userPrincipal.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterWithoutHeaderContinuesChainUnauthenticated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}

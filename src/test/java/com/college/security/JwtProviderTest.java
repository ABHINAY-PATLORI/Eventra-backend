package com.college.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    @Test
    void shouldGenerateAndValidateToken() {
        JwtProvider jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret",
                "this-is-a-test-jwt-secret-key-with-enough-length-1234567890");
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", 60000L);

        String token = jwtProvider.generateToken("student@college.com");

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("student@college.com", jwtProvider.getEmailFromToken(token));
    }

    @Test
    void shouldRejectMalformedToken() {
        JwtProvider jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret",
                "this-is-a-test-jwt-secret-key-with-enough-length-1234567890");
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", 60000L);

        assertFalse(jwtProvider.validateToken("invalid-token"));
    }
}

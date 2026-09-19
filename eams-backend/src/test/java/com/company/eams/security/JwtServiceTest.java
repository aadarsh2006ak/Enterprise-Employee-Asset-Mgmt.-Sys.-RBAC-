package com.company.eams.security;

import com.company.eams.entity.enums.RoleType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L); // 15 mins
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L); // 7 days
        ReflectionTestUtils.setField(jwtService, "cookieName", "eams_refresh_token");
    }

    @Test
    @DisplayName("Generate access token and extract username and claims successfully")
    void testGenerateAccessTokenAndExtractClaims() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(100L)
                .username("john_doe")
                .email("john@company.com")
                .role(RoleType.EMPLOYEE)
                .permissions(Set.of("ASSET_READ", "ASSET_REQUEST_CREATE"))
                .active(true)
                .authorities(Collections.emptyList())
                .build();

        String token = jwtService.generateAccessToken(principal);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.validateToken(token));
        assertEquals("john_doe", jwtService.extractUsername(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(100, ((Number) claims.get("userId")).longValue());
        assertEquals("EMPLOYEE", claims.get("role"));
        assertEquals("john@company.com", claims.get("email"));
    }

    @Test
    @DisplayName("Generate raw refresh token and hash successfully")
    void testGenerateRawRefreshTokenAndHash() {
        String rawToken = jwtService.generateRawRefreshToken();
        assertNotNull(rawToken);
        assertFalse(rawToken.isEmpty());

        String hash1 = jwtService.hashToken(rawToken);
        String hash2 = jwtService.hashToken(rawToken);
        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Hashing same token should produce identical hash");
    }

    @Test
    @DisplayName("Validate invalid token returns false")
    void testInvalidTokenValidation() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtService.validateToken(invalidToken));
    }
}

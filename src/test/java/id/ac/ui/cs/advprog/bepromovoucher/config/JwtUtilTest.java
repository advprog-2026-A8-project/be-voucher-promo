package id.ac.ui.cs.advprog.bepromovoucher.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "TestSecretKeyUntukIntegrationTestYangCukupPanjang123!@#";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    private String generateToken(String username, String role, boolean expired) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        long expiration = expired ? -10000L : 86400000L;

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration));

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(key).compact();
    }

    @Test
    void testValidateTokenSuccess() {
        String token = generateToken("adminUser", "ADMIN", false);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenExpiredReturnsFalse() {
        String token = generateToken("adminUser", "ADMIN", true);
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenInvalidReturnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void testExtractRoleAdmin() {
        String token = generateToken("adminUser", "ADMIN", false);
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void testExtractRoleUser() {
        String token = generateToken("regularUser", "USER", false);
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void testExtractUsername() {
        String token = generateToken("adminUser", "ADMIN", false);
        assertEquals("adminUser", jwtUtil.extractUsername(token));
    }

    @Test
    void testExtractRoleNullWhenNoRoleClaim() {
        String token = generateToken("adminUser", null, false);
        assertNull(jwtUtil.extractRole(token));
    }
}
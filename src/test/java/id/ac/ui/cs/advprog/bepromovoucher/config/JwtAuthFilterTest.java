package id.ac.ui.cs.advprog.bepromovoucher.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private JwtUtil jwtUtil;

    private static final String SECRET =
            "TestSecretKeyUntukIntegrationTestYangCukupPanjang123!@#";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);

        jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        ReflectionTestUtils.setField(jwtAuthFilter, "disableFilter", false);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String generateToken(String username, String role, boolean expired) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        long expiration = expired ? -10000L : 86400000L;

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 20000L))
                .expiration(new Date(System.currentTimeMillis() + expiration));

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(key).compact();
    }

    @Test
    void testFilterSetsSecurityContextWithValidAdminJwt() throws Exception {
        String token = generateToken("adminUser", "ADMIN", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("adminUser", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testFilterSetsSecurityContextWithValidUserJwt() throws Exception {
        String token = generateToken("regularUser", "USER", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/use");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("regularUser", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testFilterLeavesContextNullWhenNoTokenProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterLeavesContextNullWhenTokenExpired() throws Exception {
        String token = generateToken("adminUser", "ADMIN", true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterLeavesContextNullWhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("Authorization", "Bearer invalid.token.here");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testFilterLeavesContextNullWhenNonBearerAuthProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDisableFilterSkipsTokenProcessing() throws Exception {
        jwtAuthFilter.setDisableFilter(true);
        String token = generateToken("adminUser", "ADMIN", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
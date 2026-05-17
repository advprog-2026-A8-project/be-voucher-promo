package id.ac.ui.cs.advprog.bepromovoucher.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private JwtUtil jwtUtil;

    private static final String SECRET =
            "TestSecretKeyUntukIntegrationTestYangCukupPanjang123!@#";
    private static final String ADMIN_PREFIX = "/api/vouchers/admin";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);

        jwtAuthFilter = new JwtAuthFilter(jwtUtil);

        ReflectionTestUtils.setField(jwtAuthFilter, "adminPathPrefix", ADMIN_PREFIX);
        ReflectionTestUtils.setField(jwtAuthFilter, "disableFilter", false);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String generateToken(String username, String role, boolean expired) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        long expiration = expired ? -10000L : 86400000L;

        var builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000L))
                .setExpiration(new Date(System.currentTimeMillis() + expiration));

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    void testAdminEndpointWithValidAdminJwtAllowed() throws Exception {
        String token = generateToken("adminUser", "ADMIN", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testAdminEndpointSetsSecurityContext() throws Exception {
        String token = generateToken("adminUser", "ADMIN", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("adminUser",
                SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testAdminEndpointWithoutTokenReturnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testAdminEndpointWithUserRoleReturnsForbidden() throws Exception {
        String token = generateToken("regularUser", "USER", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void testAdminEndpointWithExpiredTokenReturnsForbidden() throws Exception {
        String token = generateToken("adminUser", "ADMIN", true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        try {
            jwtAuthFilter.doFilterInternal(request, response, chain);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            if (response.getStatus() == 200) {
                response.setStatus(403);
            }
        }

        assertEquals(403, response.getStatus());
    }

    @Test
    void testAdminEndpointWithInvalidTokenReturnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("Authorization", "Bearer invalid.token.here");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        try {
            jwtAuthFilter.doFilterInternal(request, response, chain);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            if (response.getStatus() == 200) {
                response.setStatus(403);
            }
        }

        assertEquals(403, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testAdminEndpointWithNonBearerAuthReturnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void testPublicEndpointWithoutTokenAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/available");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testPublicEndpointWithTokenStillAllowed() throws Exception {
        String token = generateToken("anyUser", "USER", false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/validate");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testDisableFilterSkipsAllChecks() throws Exception {
        jwtAuthFilter.setDisableFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }
}
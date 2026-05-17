package id.ac.ui.cs.advprog.bepromovoucher.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter();
    }

    @Test
    void testAdminEndpointWithAdminRoleAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");
        request.addHeader("X-User-Role", "ADMIN");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testAdminEndpointWithoutRoleReturnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/list");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void testAdminEndpointWithUserRoleReturnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/admin/create");
        request.addHeader("X-User-Role", "USER");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void testPublicEndpointWithoutRoleAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/vouchers/available");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }
}
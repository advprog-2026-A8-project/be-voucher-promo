package id.ac.ui.cs.advprog.bepromovoucher.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String HEADER_ROLE = "X-User-Role";
    private static final String ADMIN_PATH_PREFIX = "/api/vouchers/admin";

    @Value("${app.security.disable-filter:false}")
    private boolean disableFilter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (disableFilter) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (path.startsWith(ADMIN_PATH_PREFIX)) {
            String role = request.getHeader(HEADER_ROLE);

            if (role == null || !role.equals(ADMIN_ROLE)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\": \"Akses ditolak: hanya admin yang diizinkan\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
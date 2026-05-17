package id.ac.ui.cs.advprog.bepromovoucher.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ADMIN_PATH_PREFIX = "/api/vouchers/admin";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Setter
    @Value("${app.security.disable-filter:false}")
    private boolean disableFilter;

    private List<SimpleGrantedAuthority> toAuthorities(String role) {
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return List.of(new SimpleGrantedAuthority("ROLE_" + normalized));
    }

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

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                List<SimpleGrantedAuthority> authorities = toAuthorities(role);

                if (!authorities.isEmpty()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authenticated user: {} with role: {}", username, role);
                }
            }
        }

        if (path.startsWith(ADMIN_PATH_PREFIX)) {
            var auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || auth.getAuthorities().stream()
                    .noneMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_" + ADMIN_ROLE))) {
                sendForbidden(response, "Akses ditolak: hanya admin yang diizinkan");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
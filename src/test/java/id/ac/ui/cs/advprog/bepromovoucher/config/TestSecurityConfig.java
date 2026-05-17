package id.ac.ui.cs.advprog.bepromovoucher.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return new JwtUtil() {
            @Override
            public boolean validateToken(String token) {
                return false;
            }

            @Override
            public String extractUsername(String token) {
                return null;
            }

            @Override
            public String extractRole(String token) {
                return null;
            }
        };
    }

    @Bean
    @Primary
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil) {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http,
                                               JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/vouchers/available",
                                "/api/vouchers/validate",
                                "/api/vouchers/use",
                                "/api/vouchers/restore"
                        ).permitAll()
                        .requestMatchers("/api/vouchers/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
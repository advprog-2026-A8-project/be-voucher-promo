package id.ac.ui.cs.advprog.bepromovoucher.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class JwtUtilBenchmark {

    private static final String SECRET =
            "TestSecretKeyUntukIntegrationTestYangCukupPanjang123!@#";

    private JwtUtil jwtUtilNoCache;
    private JwtUtil jwtUtilWithCache;
    private String validToken;

    private void setSecretField(JwtUtil target, String secretValue) {
        try {
            Field field = JwtUtil.class.getDeclaredField("secret");
            field.setAccessible(true);
            field.set(target, secretValue);
        } catch (Exception e) {
            throw new RuntimeException("Gagal set secret via reflection", e);
        }
    }

    @Setup(Level.Trial)
    public void setUp() {
        jwtUtilNoCache = new JwtUtil();
        setSecretField(jwtUtilNoCache, SECRET);

        jwtUtilWithCache = new JwtUtil();
        setSecretField(jwtUtilWithCache, SECRET);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        validToken = Jwts.builder()
                .subject("benchmarkUser")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000L))
                .signWith(key)
                .compact();

        jwtUtilWithCache.validateToken(validToken);
    }

    @Setup(Level.Invocation)
    public void clearCacheBeforeEachNoCache() {
        jwtUtilNoCache.getClaimsCache().invalidateAll();
    }

    @Benchmark
    public boolean validateToken_withoutCache() {
        return jwtUtilNoCache.validateToken(validToken);
    }

    @Benchmark
    public boolean validateToken_withCache() {
        return jwtUtilWithCache.validateToken(validToken);
    }
}
package com.example.security;

import com.example.exception.TokenNotFoundException;
import com.example.model.AccountActivationToken;
import com.example.model.User;
import com.example.repository.AccountActivationTokenRepository;
import com.example.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final AccountActivationTokenRepository authTokenRepository;
    private final UserRepository userRepository;

    @Value("${activation.token.expiration}")
    private int activationTokenExpiration;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.issuer}")
    private String jwtIssuer;
    @Value("${token.expiration}")
    private int tokenExpiration;

    public String generateAccountActivationToken(User user) {
        String token = UUID.randomUUID().toString();
        AccountActivationToken accountActivationToken = new AccountActivationToken(
                user, token, Instant.now().plus(activationTokenExpiration, ChronoUnit.MILLIS));
        authTokenRepository.save(accountActivationToken);

        return token;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(format("%s,%s", user.getId(), user.getUsername()))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(Instant.now()
                        .plus(tokenExpiration, ChronoUnit.MILLIS).toEpochMilli())) // 8 hours
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void enableUser(String token) {
        AccountActivationToken authToken = authTokenRepository.findAll().stream()
                .filter(t -> t.getToken().equals(token)).findAny()
                .orElseThrow(TokenNotFoundException::new);
        tokenExpired(authToken);

        User user = authToken.getUser();
        if (user == null)
            throw new UsernameNotFoundException("Could not find user");

        user.setEnabled(true);
        userRepository.save(user);
        authToken.setModifiedAt(Instant.now());

        log.info("user has been enabled");
    }

    private void tokenExpired(AccountActivationToken authToken) {
        long expirationDate = authToken.getCreatedAt().toEpochMilli() + tokenExpiration;
        long currentDate = Instant.now().toEpochMilli();

        if (expirationDate - currentDate < 0) {
            authTokenRepository.delete(authToken);
            userRepository.delete(authToken.getUser());

            throw new TokenNotFoundException("Token expired");
        }
    }
}

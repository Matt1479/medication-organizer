package com.matt1479.medicationorganizer.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.matt1479.medicationorganizer.users.CustomUserDetails;

import jakarta.validation.constraints.Min;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Validated 
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long ttlMinutes;

    public TokenService(JwtEncoder jwtEncoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.ttl-minutes}") @Min(1) long ttlMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(getUserId(authentication))
                .claim("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getTtlSeconds() {
        return ttlMinutes * 60;
    }

    private String getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            return userDetails.getId().toString();
        }

        throw new IllegalArgumentException(
                "Authentication principal must be CustomUserDetails");
    }

}

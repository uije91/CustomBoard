package com.zerobase.customboard.global.jwt;

import com.zerobase.customboard.global.jwt.dto.TokenDto;
import com.zerobase.customboard.infra.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secretKey;
  private final RedisService redisService;
  private final CustomUserDetailsService userDetailsService;
  private static final String TOKEN_PREFIX = "Bearer";
  private static final long ACCESS_EXPIRATION_TIME = 30 * 60 * 1000L; // 30분
  private static final long REFRESH_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000L; // 14일

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // 토큰 생성
  public TokenDto generateToken(Authentication authentication) {
    String email = authentication.getName();
    String role = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    Date now = new Date();

    String accessToken = Jwts.builder()
        .setExpiration(new Date(now.getTime() + ACCESS_EXPIRATION_TIME))
        .setSubject("access")
        .claim("email", email)
        .claim("role", role)
        .signWith(getKey(), SignatureAlgorithm.HS512)
        .compact();

    String refreshToken = Jwts.builder()
        .setExpiration(new Date(now.getTime() + REFRESH_EXPIRATION_TIME))
        .setSubject("refresh")
        .signWith(getKey(), SignatureAlgorithm.HS512)
        .compact();

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public boolean validateToken(String token) {
    try {
      if (redisService.getData(token) != null && !redisService.getData(token).equals("blackList")) {
        return false;
      }
      Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.error("[JwtUtil] Invalid JWT Token : {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("[JwtUtil] Expired JWT Token : {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("[JwtUtil] Unsupported JWT Token : {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("[JwtUtil] JWT claims string is empty : {}", e.getMessage());
    }
    return false;
  }

  public Authentication getAuthentication(String accessToken) {
    String email = getClaims(accessToken).get("email").toString();

    CustomUserDetails principal = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
    log.info("[JwtUtil] 토큰 인증 정보 조회 완료 : {}", principal.getUsername());
    return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
  }

  public String resolveToken(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
      return token.substring(7);
    }
    return null;
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  public long getExpirationTime(String token) {
    if (token.startsWith(TOKEN_PREFIX)) {
      token = token.substring(7);
    }
    return getClaims(token).getExpiration().getTime() - new Date().getTime();
  }
}

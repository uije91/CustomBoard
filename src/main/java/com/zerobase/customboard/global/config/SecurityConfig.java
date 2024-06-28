package com.zerobase.customboard.global.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.zerobase.customboard.global.type.Role;
import com.zerobase.customboard.global.jwt.JwtAuthenticationFilter;
import com.zerobase.customboard.global.jwt.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtUtil jwtUtil;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/admin/**").hasAnyAuthority(Role.ADMIN.getKey())
            .requestMatchers(requestAuthenticated()).authenticated()
            .requestMatchers(anyRequest()).permitAll())
        .sessionManagement(configurer -> configurer
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
            UsernamePasswordAuthenticationFilter.class)

    ;
    return http.build();
  }

  // 모든 사용자 접근 가능 경로
  private RequestMatcher[] anyRequest() {
    List<RequestMatcher> requestMatchers = List.of(
        antMatcher("/"),
        antMatcher("/api-test"),
        antMatcher("/swagger-ui/**"),
        antMatcher("/v3/api-docs/**"),
        antMatcher("/api/email/**"),
        antMatcher(POST, "/api/member/signup"),
        antMatcher(POST, "/api/member/login"),
        antMatcher(POST, "/api/member/reissue"),
        antMatcher(PUT, "/api/member/resign"),
        antMatcher("/api/member/password/*"),
        antMatcher(GET, "/api/post/*"),
        antMatcher(POST, "/api/post/*"),
        antMatcher(GET, "/api/comment/*"),
        antMatcher("/ws/**")
    );
    return requestMatchers.toArray(RequestMatcher[]::new);
  }

  // 유저, 관리자 모두 접근 가능
  private RequestMatcher[] requestAuthenticated() {
    List<RequestMatcher> requestMatchers = List.of(
        antMatcher(POST, "/api/member/logout"),
        antMatcher(PUT,"/api/post/*"),
        antMatcher(POST,"/api/post"),
        antMatcher("/api/post/likes/*"),
        antMatcher("/api/member/profile"),
        antMatcher("/api/comment"),
        antMatcher(POST,"/api/comment/likes/*")
    );
    return requestMatchers.toArray(RequestMatcher[]::new);
  }

  // 비밀번호 암호화
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

# Trouble Shooting
프로젝트를 진행하면서 발생한 문제점들과 해결법 서술합니다.

## Swagger 접근 불가 문제점

---
### 원인
- SecurityConfig에 로그인 사용을 위해 아래 필터를 적용한 후부터 스웨거 페이지 접근 불가
~~~java
.addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
            UsernamePasswordAuthenticationFilter.class)
~~~

### 해결
- 스웨거의 접근경로를 permitAll()로 허용
~~~java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(anyRequest()).permitAll()

private RequestMatcher[] anyRequest() {
    List<RequestMatcher> requestMatchers = List.of(
        antMatcher("/"),
        antMatcher("/api-test"),
        antMatcher("/swagger-ui/**")
    );
  return requestMatchers.toArray(RequestMatcher[]::new);
}
~~~
---

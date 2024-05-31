package com.zerobase.customboard.global.jwt;

import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return memberRepository.findByEmail(email)
        .map(CustomUserDetails::new)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }
}

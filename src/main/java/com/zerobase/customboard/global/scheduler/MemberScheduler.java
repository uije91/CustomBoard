package com.zerobase.customboard.global.scheduler;

import static com.zerobase.customboard.domain.member.type.Status.RESIGN;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.member.service.MemberService;
import com.zerobase.customboard.domain.member.type.Status;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class MemberScheduler {

  private final MemberRepository memberRepository;

  @Scheduled(cron = "0 0 0 * * *")
  public void removeResignMember() {
    LocalDateTime timePassed = LocalDateTime.now().minusDays(30);

    List<Member> members = memberRepository.findByStatusAndUpdatedAtBefore(RESIGN, timePassed);
    memberRepository.deleteAll(members);
  }

}

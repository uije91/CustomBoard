package com.zerobase.customboard.domain.member.repository;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.global.type.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  boolean existsByNickname(String nickname);

  List<Member> findByStatusAndUpdatedAtBefore(Status status, LocalDateTime updatedAt);

}

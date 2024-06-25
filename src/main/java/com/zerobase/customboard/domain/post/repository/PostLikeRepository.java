package com.zerobase.customboard.domain.post.repository;

import com.zerobase.customboard.domain.post.entity.PostLike;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  @EntityGraph(attributePaths = {"member","post"})
  Optional<PostLike> findByMemberIdAndPostId(Long memberId,Long postId);

  @EntityGraph(attributePaths = "member")
  Page<PostLike> findByMemberId(Long memberId, Pageable pageable);
}

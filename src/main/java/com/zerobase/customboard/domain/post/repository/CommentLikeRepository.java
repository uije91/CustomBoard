package com.zerobase.customboard.domain.post.repository;

import com.zerobase.customboard.domain.post.entity.CommentLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  @EntityGraph(attributePaths = {"member,comment"})
  Optional<CommentLike> findByMemberIdAndCommentId(Long memberId,Long commentId);
}

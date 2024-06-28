package com.zerobase.customboard.domain.post.repository;

import com.zerobase.customboard.domain.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  Page<Comment> findByPostId(Long postId, Pageable pageable);
}

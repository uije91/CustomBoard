package com.zerobase.customboard.domain.post.service;

import static com.zerobase.customboard.global.exception.ErrorCode.COMMENT_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.DO_NOT_HAVE_PERMISSION;
import static com.zerobase.customboard.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.zerobase.customboard.global.type.Status.ACTIVE;
import static com.zerobase.customboard.global.type.Status.INACTIVE;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.CommentDto.getCommentDto;
import com.zerobase.customboard.domain.post.dto.CommentDto.writeCommentDto;
import com.zerobase.customboard.domain.post.entity.Comment;
import com.zerobase.customboard.domain.post.entity.CommentLike;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.repository.CommentLikeRepository;
import com.zerobase.customboard.domain.post.repository.CommentRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;

  public Page<getCommentDto> getComments(Long postId, Pageable pageable) {
    Page<Comment> commentPage = commentRepository.findByPostId(postId,pageable);

    List<getCommentDto> commentList = commentPage.getContent().stream()
        .filter(item -> item.getStatus() == ACTIVE)
        .map(getCommentDto::to)
        .collect(Collectors.toList());

    return new PageImpl<>(commentList,pageable,commentPage.getTotalElements());
  }

  @Transactional
  public void createComment(Long memberId, writeCommentDto comment) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Post post = postRepository.findById(comment.getPostId())
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    commentRepository.save(Comment.builder()
        .contents(comment.getContents())
        .post(post)
        .member(member)
        .build());
  }

  @Transactional
  public void deleteComment(Long memberId, Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));

    if (!comment.getMember().getId().equals(memberId)) {
      throw new CustomException(DO_NOT_HAVE_PERMISSION);
    }
    comment.changeStatus(INACTIVE);
    commentRepository.save(comment);
  }

  @Transactional
  public boolean setCommentLike(Long memberId, Long commentId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));

    Optional<CommentLike> commentLike = commentLikeRepository.findByMemberIdAndCommentId(
        memberId, commentId);
    if (commentLike.isPresent()) {
      removeInterest(commentLike.get().getId());
      return false;
    }
    addInterest(member, comment);
    return true;
  }

  private void addInterest(Member member, Comment comment) {
    commentLikeRepository.save(CommentLike.builder().member(member).comment(comment).build());
  }

  private void removeInterest(Long commentId) {
    commentLikeRepository.deleteById(commentId);
  }
}

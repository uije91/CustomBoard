package com.zerobase.customboard.domain.post.service;

import static com.zerobase.customboard.global.exception.ErrorCode.DO_NOT_HAVE_PERMISSION;
import static com.zerobase.customboard.global.type.Status.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.CommentDto;
import com.zerobase.customboard.domain.post.dto.CommentDto.getCommentDto;
import com.zerobase.customboard.domain.post.dto.CommentDto.writeCommentDto;
import com.zerobase.customboard.domain.post.dto.PostLikeDto;
import com.zerobase.customboard.domain.post.entity.Comment;
import com.zerobase.customboard.domain.post.entity.CommentLike;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.entity.PostLike;
import com.zerobase.customboard.domain.post.repository.CommentLikeRepository;
import com.zerobase.customboard.domain.post.repository.CommentRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.entity.BaseEntity;
import com.zerobase.customboard.global.exception.CustomException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @InjectMocks
  CommentService commentService;

  @Mock
  PostRepository postRepository;
  @Mock
  MemberRepository memberRepository;
  @Mock
  CommentRepository commentRepository;
  @Mock
  CommentLikeRepository commentLikeRepository;


  private Member member;
  private Post post;

  @BeforeEach
  void setup() {
    member = Member.builder()
        .id(1L)
        .nickname("testUser")
        .build();

    post = Post.builder()
        .id(10L)
        .build();
  }

  @Test
  @DisplayName("댓글 등록 성공")
  void testCreateComment_success() {
    // given
    writeCommentDto comment = writeCommentDto.builder()
        .postId(post.getId())
        .contents("this is a test comment")
        .build();

    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

    // when
    commentService.createComment(member.getId(),comment);

    //then
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 삭제 성공")
  void testDeleteComment_success() {
    Comment comment = Comment.builder().id(1L).member(member).build();

    // given
    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    // when
    commentService.deleteComment(member.getId(), comment.getId());

    //then
    assertEquals(INACTIVE, comment.getStatus());
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 작성자가 아님")
  void testDeleteComment_fail_doNotHavePermission() {
    Long otherMemberId = 2L;
    Comment comment = Comment.builder().id(1L).member(member).build();

    // given
    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> commentService.deleteComment(otherMemberId, comment.getId()));

    //then
    assertEquals(DO_NOT_HAVE_PERMISSION, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 목록 보기")
  void testGetComments() throws Exception {
    // given
    Pageable pageable = PageRequest.of(0, 50);
    Comment comment = Comment.builder()
        .id(1L)
        .contents("this is comment!")
        .member(member).post(post).likes(Set.of()).build();

    // BaseEntity 시간 강제 설정
    LocalDateTime time = LocalDateTime.of(2024, 6, 1, 15, 30);
    Field createdField = BaseEntity.class.getDeclaredField("createdAt");
    createdField.setAccessible(true);
    createdField.set(comment, time);

    Page<Comment> commmentPage = new PageImpl<>(List.of(comment));
    given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(commmentPage);

    // when
    Page<getCommentDto> result = commentService.getComments(post.getId(), pageable);

    //then
    assertEquals(1, result.getTotalElements());
    assertEquals(result.getContent().get(0).getWriter(), member.getNickname());
    assertEquals("this is comment!",result.getContent().get(0).getContents());
    assertEquals("testUser",result.getContent().get(0).getWriter());
    assertEquals("2024.06.01 15:30:00",result.getContent().get(0).getCommentTime());
  }


  @Test
  @DisplayName("댓글 좋아요 - 추가")
  void testSetPostLikes_addLike() {
    // given
    Comment comment = Comment.builder().id(1L).member(member).build();

    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
    given(commentLikeRepository.findByMemberIdAndCommentId(member.getId(), comment.getId()))
        .willReturn(Optional.empty());

    // when
    boolean result = commentService.setCommentLike(member.getId(), comment.getId());

    //then
    assertTrue(result);
    verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
  }

  @Test
  @DisplayName("댓글 좋아요 - 삭제")
  void testSetPostLikes_removeLike() {
    // given
    Member member = Member.builder().id(1L).nickname("testUser").build();
    Comment comment = Comment.builder().id(1L).member(member).build();
    CommentLike commentLike = CommentLike.builder().id(1L).member(member).build();

    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
    given(commentLikeRepository.findByMemberIdAndCommentId(member.getId(), comment.getId()))
        .willReturn(Optional.of(commentLike));

    // when
    boolean result = commentService.setCommentLike(member.getId(), comment.getId());

    //then
    assertFalse(result);
    verify(commentLikeRepository, times(1)).deleteById(commentLike.getId());
  }
}
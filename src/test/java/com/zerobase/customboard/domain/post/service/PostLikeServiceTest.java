package com.zerobase.customboard.domain.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.PostLikeDto;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.entity.PostLike;
import com.zerobase.customboard.domain.post.repository.PostLikeRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.entity.BaseEntity;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
class PostLikeServiceTest {

  @InjectMocks
  private PostLikeService postLikeService;
  @Mock
  private PostLikeRepository postLikeRepository;
  @Mock
  private PostRepository postRepository;
  @Mock
  private MemberRepository memberRepository;

  @Test
  @DisplayName("좋아요한 게시글 목록 보기")
  void testGetPostLikes() throws Exception {
    // given
    Member member = Member.builder().id(1L).nickname("testUser").build();
    Post post = Post.builder()
        .id(1L)
        .title("Test Post")
        .views(100)
        .likes(new LinkedHashSet<>())
        .member(member)
        .build();

    Pageable pageable = PageRequest.of(0, 10);

    // BaseEntity 시간 강제 설정
    LocalDateTime time = LocalDateTime.of(2024, 6, 1, 15, 30);
    Field createdField = BaseEntity.class.getDeclaredField("createdAt");
    createdField.setAccessible(true);
    createdField.set(post, time);

    Page<PostLike> postLikePage = new PageImpl<>(
        List.of(PostLike.builder().id(1L).member(member).post(post).build()), pageable, 1);
    given(postLikeRepository.findByMemberId(member.getId(), pageable)).willReturn(postLikePage);

    // when
    Page<PostLikeDto> result = postLikeService.getPostLikes(member.getId(), pageable);

    //then
    assertEquals(1, result.getTotalElements());
    assertEquals(result.getContent().get(0).getWriter(), member.getNickname());
    assertEquals("Test Post", result.getContent().get(0).getTitle());
    assertEquals("2024.06.01 15:30:00",result.getContent().get(0).getPostTime());
    assertEquals(100, result.getContent().get(0).getView());
  }

  @Test
  @DisplayName("게시글 좋아요 - 좋아요 추가")
  void testSetPostLikes_addLike() {
    // given
    Long memberId = 1L;
    Long postId = 1L;

    Member member = Member.builder().id(memberId).nickname("testUser").build();
    Post post = Post.builder().id(postId).title("Test Post").build();

    given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
    given(postRepository.findById(postId)).willReturn(Optional.of(post));
    given(postLikeRepository.findByMemberIdAndPostId(memberId, postId))
        .willReturn(Optional.empty());

    // when
    boolean result = postLikeService.setPostLike(memberId, postId);

    //then
    assertTrue(result);
    verify(postLikeRepository, times(1)).save(any(PostLike.class));
  }

  @Test
  @DisplayName("게시글 좋아요 - 좋아요 삭제")
  void testSetPostLikes_removeLike() {
    // given
    Long memberId = 1L;
    Long postId = 1L;

    Member member = Member.builder().id(memberId).nickname("testUser").build();
    Post post = Post.builder().id(postId).title("Test Post").build();
    PostLike postLike = PostLike.builder().id(1L).member(member).post(post).build();

    given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
    given(postRepository.findById(postId)).willReturn(Optional.of(post));
    given(postLikeRepository.findByMemberIdAndPostId(memberId, postId))
        .willReturn(Optional.of(postLike));

    // when
    boolean result = postLikeService.setPostLike(memberId, postId);

    //then
    assertFalse(result);
    verify(postLikeRepository, times(1)).deleteById(postLike.getId());
  }

}
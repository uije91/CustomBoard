package com.zerobase.customboard.domain.post.service;

import static com.zerobase.customboard.global.exception.ErrorCode.DO_NOT_HAVE_PERMISSION;
import static com.zerobase.customboard.global.type.Status.INACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.admin.repository.BoardRepository;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.PostDto;
import com.zerobase.customboard.domain.post.dto.PostDto.writePostDto;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.repository.ImageRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.exception.ErrorCode;
import com.zerobase.customboard.global.type.Status;
import com.zerobase.customboard.infra.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @InjectMocks
  private PostService postService;

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private BoardRepository boardRepository;
  @Mock
  private PostRepository postRepository;
  @Mock
  private ImageRepository imageRepository;

  private Member member;
  private Board board;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .id(1L)
        .build();

    board = Board.builder()
        .id(10L)
        .boardName("자유게시판")
        .build();
  }


  @Test
  @DisplayName("게시글 등록 성공")
  void testWritePost_success() throws Exception {
    // given
    List<MultipartFile> files = new ArrayList<>();
    files.add(
        new MockMultipartFile("postFiles1", "test1.png", "png", "test file content".getBytes()));

    PostDto.writePostDto post = writePostDto.builder()
        .title("테스트 게시글 제목")
        .contents("테스트 게시글 내용")
        .postFiles(files)
        .boardId(board.getId()).build();

    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

    // when
    postService.writePost(member.getId(), post);

    //then
    verify(postRepository, times(1)).save(any(Post.class));
    verify(imageRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 삭제 성공")
  void testDeletePost_success() {
    Post post = Post.builder()
        .id(1L)
        .member(member)
        .build();

    // given
    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(postRepository.findById(any())).willReturn(Optional.of(post));

    // when
    postService.deletePost(member.getId(), post.getId());

    //then
    assertEquals(INACTIVE, post.getStatus());
  }

  @Test
  @DisplayName("게시글 삭제 실패 - 게시글 작성자가 아님")
  void testDeletePost_fail_doNotHavePermission() {

    Member member2 = new Member();
    Post post = Post.builder()
        .id(1L)
        .member(member2)
        .build();

    // given
    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(postRepository.findById(any())).willReturn(Optional.of(post));

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> postService.deletePost(member.getId(), post.getId()));

    //then
    assertEquals(DO_NOT_HAVE_PERMISSION,exception.getErrorCode());
  }

}
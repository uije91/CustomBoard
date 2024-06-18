package com.zerobase.customboard.domain.post.service;

import static com.zerobase.customboard.global.exception.ErrorCode.BOARD_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.DO_NOT_HAVE_PERMISSION;
import static com.zerobase.customboard.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.zerobase.customboard.global.type.Status.INACTIVE;

import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.admin.repository.BoardRepository;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.PostDto.writePostDto;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.entity.PostImage;
import com.zerobase.customboard.domain.post.repository.ImageRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.infra.service.S3Service;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final S3Service s3Service;
  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final BoardRepository boardRepository;
  private final ImageRepository imageRepository;


  @Transactional
  public void writePost(Long memberId, writePostDto writePostDto) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Board board = boardRepository.findById(writePostDto.getBoardId())
        .orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));

    Post post = Post.builder()
        .title(writePostDto.getTitle())
        .contents(writePostDto.getContents())
        .member(member)
        .board(board)
        .build();
    postRepository.save(post);

    List<PostImage> images = writePostDto.getPostFiles().stream()
        .filter(file -> Objects.requireNonNull(file.getOriginalFilename()).isEmpty())
        .map(file -> {
          String filePath = s3Service.uploadFile(file, member.getEmail() + "/postImage");
          return PostImage.builder()
              .path(filePath)
              .post(post)
              .build();
        })
        .toList();
    imageRepository.saveAll(images);
  }

  public void deletePost(Long memberId, Long postId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    if(!post.getMember().equals(member)) {
      throw new CustomException(DO_NOT_HAVE_PERMISSION);
    }
    post.changeStatus(INACTIVE);
    postRepository.save(post);
  }
}

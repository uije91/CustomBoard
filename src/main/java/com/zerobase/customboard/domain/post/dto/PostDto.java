package com.zerobase.customboard.domain.post.dto;

import com.zerobase.customboard.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class PostDto {

  @Getter
  @Builder
  @Schema(name = "게시글 작성")
  public static class writePostDto {

    private List<MultipartFile> postFiles;

    @NotBlank(message = "게시글 제목은 필수 항목입니다.")
    private String title;

    @NotBlank(message = "게시글 내용은 필수 항목입니다.")
    private String contents;
    private Long boardId;
  }

  @Getter
  @Builder
  @Schema(name = "게시글 보기")
  public static class getPostDto {
    private List<String> postImages;
    private String title;
    private String contents;
    private String writer;
    private String postTime;
    private int view;
    private int likes;
  }

  @Getter
  @Builder
  public static class postListDto {

    private Long postId;
    private String title;
    private String writer;
    private String createdAt;
    private int view;
    private int likes;

    public static postListDto to(Post post) {
      String postTime = post.getCreatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate())
          ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm"))
          : post.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd"));

      return postListDto.builder()
          .postId(post.getId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .createdAt(postTime)
          .view(post.getViews())
          .likes(post.getLikes().size())
          .build();
    }
  }
}

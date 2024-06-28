package com.zerobase.customboard.domain.post.dto;

import com.zerobase.customboard.domain.post.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

public class CommentDto {

  @Getter
  @Builder
  @Schema(name = "댓글 작성")
  public static class writeCommentDto {

    @NotBlank(message = "댓글 내용은 필수 항목입니다.")
    private String contents;
    private Long postId;
  }

  @Getter
  @Builder
  @Schema(name = "댓글 목록 조회")
  public static class getCommentDto {
    private Long commentId;
    private String contents;
    private String writer;
    private String commentTime;
    private int likes;

    public static getCommentDto to(Comment comment) {
      return getCommentDto.builder()
          .commentId(comment.getId())
          .contents(comment.getContents())
          .writer(comment.getMember().getNickname())
          .commentTime(comment.getCreatedAt()
              .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")))
          .likes(comment.getLikes().size())
          .build();
    }
  }
}

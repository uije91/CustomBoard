package com.zerobase.customboard.domain.post.dto;

import com.zerobase.customboard.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "좋아요한 게시글 목록 보기")
public class PostLikeDto {
  private Long postId;
  private String title;
  private String writer;
  private String postTime;
  private int view;
  private int likes;

  public static PostLikeDto to(Post post) {
    return PostLikeDto.builder()
        .postId(post.getId())
        .title(post.getTitle())
        .writer(post.getMember().getNickname())
        .postTime(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")))
        .view(post.getViews())
        .likes(post.getLikes().size())
        .build();
  }
}

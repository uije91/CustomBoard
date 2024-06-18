package com.zerobase.customboard.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class PostDto {

  @Getter
  @Builder
  public static class writePostDto{
    private List<MultipartFile> postFiles;

    @NotBlank(message = "게시글 제목은 필수 항목입니다.")
    private String title;

    @NotBlank(message = "게시글 내용은 필수 항목입니다.")
    private String contents;
    private Long boardId;
  }

}

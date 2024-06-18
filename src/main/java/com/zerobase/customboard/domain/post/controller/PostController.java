package com.zerobase.customboard.domain.post.controller;

import com.zerobase.customboard.domain.post.dto.PostDto.writePostDto;
import com.zerobase.customboard.domain.post.service.PostService;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Tag(name = "Post", description = "게시글 API")
public class PostController {

  private final PostService postService;

  @Operation(summary = "게시글작성 API")
  @PostMapping("/write")
  public ResponseEntity<?> writePost(@ModelAttribute @Valid writePostDto post,
      @AuthenticationPrincipal CustomUserDetails principal) {
    postService.writePost(principal.getId(), post);
    return ResponseEntity.ok().build();
  }
}

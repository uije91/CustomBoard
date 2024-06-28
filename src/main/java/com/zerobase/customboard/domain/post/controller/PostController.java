package com.zerobase.customboard.domain.post.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.zerobase.customboard.domain.post.dto.PostDto.writePostDto;
import com.zerobase.customboard.domain.post.service.PostLikeService;
import com.zerobase.customboard.domain.post.service.PostService;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Tag(name = "Post", description = "게시글 API")
public class PostController {

  private final PostService postService;
  private final PostLikeService postLikeService;

  @Operation(summary = "게시글작성 API")
  @PostMapping()
  public ResponseEntity<?> writePost(@ModelAttribute @Valid writePostDto post,
      @AuthenticationPrincipal CustomUserDetails principal) {
    postService.createPost(principal.getId(), post);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "게시글삭제 API")
  @PutMapping("/{postId}")
  public ResponseEntity<?> deletePost(@PathVariable Long postId,
      @AuthenticationPrincipal CustomUserDetails principal) {
    postService.deletePost(principal.getId(),postId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "게시글 목록 조회 API")
  @GetMapping("/{boardId}")
  public ResponseEntity<?> getPostList(@PathVariable Long boardId,
      @PageableDefault(sort = "id", direction = DESC) Pageable pageable){
    return ResponseEntity.ok(postService.getPostList(boardId,pageable));
  }

  @Operation(summary = "게시글 조회 API")
  @PostMapping("/{postId}")
  public ResponseEntity<?> getPost(@PathVariable Long postId){
    return ResponseEntity.ok(postService.getPost(postId));
  }

  @Operation(summary = "게시글 좋아요 API")
  @PostMapping("/likes/{productId}")
  public ResponseEntity<?> setPostLike(@PathVariable Long productId,
      @AuthenticationPrincipal CustomUserDetails principal){
    return ResponseEntity.ok(postLikeService.setPostLike(principal.getId(),productId));
  }

  @Operation(summary = "게시글 좋아요 목록 조회 API")
  @GetMapping("/likes")
  public ResponseEntity<?> getPostLikes(@AuthenticationPrincipal CustomUserDetails principal,
      @PageableDefault(sort = "id", direction = DESC) Pageable pageable){
    return ResponseEntity.ok(postLikeService.getPostLikes(principal.getId(),pageable));
  }


}

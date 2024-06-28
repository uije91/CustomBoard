package com.zerobase.customboard.domain.post.controller;


import static org.springframework.data.domain.Sort.Direction.DESC;

import com.zerobase.customboard.domain.post.dto.CommentDto.writeCommentDto;
import com.zerobase.customboard.domain.post.service.CommentService;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "코멘트 API")
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "댓글 작성 API")
  @PostMapping()
  public ResponseEntity<?> createComment(@ModelAttribute writeCommentDto comment,
      @AuthenticationPrincipal CustomUserDetails principal) {
    commentService.createComment(principal.getId(), comment);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "댓글 삭제 API")
  @PutMapping()
  public ResponseEntity<?> deleteComment(@RequestParam Long commentId,
      @AuthenticationPrincipal CustomUserDetails principal) {
    commentService.deleteComment(principal.getId(), commentId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "댓글 목록 조회 API")
  @GetMapping("/{postId}")
  public ResponseEntity<?> getComment(@PathVariable Long postId,
      @PageableDefault(size = 50, sort = "id",direction = DESC) Pageable pageable) {
    return ResponseEntity.ok(commentService.getComments(postId,pageable));
  }

  @Operation(summary = "댓글 좋아요 API")
  @PostMapping("/likes/{commentId}")
  public ResponseEntity<?> setPostLike(@PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails principal){
    return ResponseEntity.ok(commentService.setCommentLike(principal.getId(),commentId));
  }
}

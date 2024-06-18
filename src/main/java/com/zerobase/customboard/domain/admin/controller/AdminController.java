package com.zerobase.customboard.domain.admin.controller;

import com.zerobase.customboard.domain.admin.dto.AdminDto.addBoardDto;
import com.zerobase.customboard.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

  private final AdminService adminService;

  @Operation(summary = "사용자 권한 변경 API",description = "사용자의 권한을 합니다.")
  @PutMapping("/role/{memberId}")
  public ResponseEntity<?> addAdmin(@PathVariable Long memberId) {
    adminService.changeRole(memberId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "게시판 추가 API",description = "게시판을 추가합니다.")
  @PostMapping("/board")
  public ResponseEntity<?> addBoard(@RequestBody addBoardDto boardDto) {
    adminService.addBoard(boardDto);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "게시판 사용유무 변경 API",description = "게시판의 사용유무를 변경합니다.")
  @PutMapping("/board")
  public ResponseEntity<?> changeBoardStatus(@RequestParam Long boardId) {
    adminService.changeBoardStatus(boardId);
    return ResponseEntity.ok().build();
  }



}

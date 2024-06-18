package com.zerobase.customboard.domain.admin.service;

import static com.zerobase.customboard.global.exception.ErrorCode.BOARD_ALREADY_EXISTS;
import static com.zerobase.customboard.global.type.Role.ADMIN;
import static com.zerobase.customboard.global.type.Role.USER;
import static com.zerobase.customboard.global.type.Status.ACTIVE;
import static com.zerobase.customboard.global.type.Status.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.zerobase.customboard.domain.admin.dto.AdminDto.addBoardDto;
import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.admin.repository.BoardRepository;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @InjectMocks
  private AdminService adminService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private BoardRepository boardRepository;

  @Test
  @DisplayName("회원 권한 변경 성공")
  void testChangeRole_success() {
    // given
    Member member = Member.builder().id(1L).role(USER).build();
    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

    // when
    adminService.changeRole(member.getId());

    //then
    assertEquals(member.getRole(), ADMIN);
  }

  @Test
  @DisplayName("게시판 생성 성공")
  void testAddBoard_success(){
    // given
    addBoardDto board = addBoardDto.builder().boardName("자유").build();
    given(boardRepository.existsByBoardName(board.getBoardName())).willReturn(false);

    // when
    adminService.addBoard(board);

    //then
    assertEquals("자유",board.getBoardName());
  }

  @Test
  @DisplayName("게시판 생성 실패 - 이미 존재하는 게시판")
  void testAddBoard_fail_boardAlreadyExists(){
    // given
    addBoardDto board = addBoardDto.builder().boardName("자유").build();
    given(boardRepository.existsByBoardName(board.getBoardName())).willReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> adminService.addBoard(board));

    //then
    assertEquals(BOARD_ALREADY_EXISTS,exception.getErrorCode());
  }

  @Test
  @DisplayName("게시판 상태 변경 성공")
  void testChangeBoardStatus_success() {
    // given
    Board board = Board.builder().boardName("자유").status(ACTIVE).build();
    given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

    // when
    adminService.changeBoardStatus(board.getId());

    //then
    assertEquals(INACTIVE, board.getStatus());
  }

}
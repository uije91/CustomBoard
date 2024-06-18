package com.zerobase.customboard.domain.admin.service;

import static com.zerobase.customboard.global.exception.ErrorCode.BOARD_ALREADY_EXISTS;
import static com.zerobase.customboard.global.exception.ErrorCode.BOARD_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.zerobase.customboard.global.type.Role.ADMIN;
import static com.zerobase.customboard.global.type.Role.USER;
import static com.zerobase.customboard.global.type.Status.ACTIVE;
import static com.zerobase.customboard.global.type.Status.INACTIVE;

import com.zerobase.customboard.domain.admin.dto.AdminDto.addBoardDto;
import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.admin.repository.BoardRepository;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.type.Role;
import com.zerobase.customboard.global.type.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final MemberRepository memberRepository;
  private final BoardRepository boardRepository;

  @Transactional
  public void changeRole(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Role newRole = (member.getRole() == USER) ? ADMIN : USER;
    member.changeRole(newRole);
    memberRepository.save(member);
  }

  public void addBoard(addBoardDto boardDto) {
    if(boardRepository.existsByBoardName(boardDto.getBoardName())){
      throw new CustomException(BOARD_ALREADY_EXISTS);
    }

    Board board = Board.builder().boardName(boardDto.getBoardName()).build();
    boardRepository.save(board);
  }

  public void changeBoardStatus(Long boardId) {
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));

    Status status = (board.getStatus() == ACTIVE) ? INACTIVE : ACTIVE;
    board.changeStatus(status);
    boardRepository.save(board);
  }
}

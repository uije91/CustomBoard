package com.zerobase.customboard.global.scheduler;

import static com.zerobase.customboard.global.type.Status.INACTIVE;

import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.admin.repository.BoardRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminScheduler {

  private final BoardRepository boardRepository;

  @Scheduled(cron = "0 0 0 * * *")
  public void removeBoard() {
    LocalDateTime timePassed = LocalDateTime.now().minusYears(1);

    List<Board> boards = boardRepository.findByStatusAndUpdatedAtBefore(INACTIVE, timePassed);
    boardRepository.deleteAll(boards);
  }

}

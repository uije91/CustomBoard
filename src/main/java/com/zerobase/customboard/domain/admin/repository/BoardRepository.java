package com.zerobase.customboard.domain.admin.repository;

import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.global.type.Status;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

  boolean existsByBoardName(String boardName);

  List<Board> findByStatusAndUpdatedAtBefore(Status status, LocalDateTime updatedAt);
}

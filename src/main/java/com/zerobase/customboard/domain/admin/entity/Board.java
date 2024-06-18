package com.zerobase.customboard.domain.admin.entity;

import static com.zerobase.customboard.global.type.Status.ACTIVE;

import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.global.entity.BaseEntity;
import com.zerobase.customboard.global.type.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "board_id")
  private Long id;

  private String boardName;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status status = ACTIVE;

  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
  private List<Post> posts = new ArrayList<>();

  public void changeStatus(Status status){
    this.status = status;
  }
}

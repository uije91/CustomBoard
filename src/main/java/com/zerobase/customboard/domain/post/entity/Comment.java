package com.zerobase.customboard.domain.post.entity;

import static com.zerobase.customboard.global.type.Status.ACTIVE;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.global.entity.BaseEntity;
import com.zerobase.customboard.global.type.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  private Long id;
  private String contents;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status status = ACTIVE;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
  private Set<CommentLike> likes;

  public void changeStatus(Status status){
    this.status = status;
  }

}

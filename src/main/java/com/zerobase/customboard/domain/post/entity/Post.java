package com.zerobase.customboard.domain.post.entity;

import static com.zerobase.customboard.global.type.Status.ACTIVE;

import com.zerobase.customboard.domain.admin.entity.Board;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.global.entity.BaseEntity;
import com.zerobase.customboard.global.type.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long id;

  private String title;
  private String contents;
  @Builder.Default
  private Long views = 0L;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Builder.Default
  private Status status = ACTIVE;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
  private Set<PostLike> likes = new LinkedHashSet<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
  private List<PostImage> images = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private Board board;
}

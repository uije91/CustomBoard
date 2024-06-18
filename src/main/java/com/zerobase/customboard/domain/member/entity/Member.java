package com.zerobase.customboard.domain.member.entity;

import static com.zerobase.customboard.global.type.Provider.LOCAL;
import static com.zerobase.customboard.global.type.Role.USER;
import static com.zerobase.customboard.global.type.Status.ACTIVE;

import com.zerobase.customboard.global.type.Provider;
import com.zerobase.customboard.global.type.Role;
import com.zerobase.customboard.global.type.Status;
import com.zerobase.customboard.domain.post.entity.Comment;
import com.zerobase.customboard.domain.post.entity.CommentLike;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.entity.PostLike;
import com.zerobase.customboard.global.entity.BaseEntity;
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

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;
  private String profileImage;
  private String email;
  private String password;
  private String nickname;
  private String mobile;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Role role = USER;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Status status = ACTIVE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Provider provider = LOCAL;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<Post> posts = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<PostLike> postLikes = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<CommentLike> commentLikes = new ArrayList<>();

  public void changeProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeNickname(String nickname) {
    this.nickname = nickname;
  }

  public void changeMobile(String mobile) {
    this.mobile = mobile;
  }

  public void changeStatus(Status status) {
    this.status = status;
  }

  public void changeRole(Role role) {
    this.role = role;
  }
}

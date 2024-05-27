package com.zerobase.customboard.domain.member.entity;

import static com.zerobase.customboard.domain.member.type.Provider.LOCAL;
import static com.zerobase.customboard.domain.member.type.Role.USER;
import static com.zerobase.customboard.domain.member.type.Status.INACTIVE;

import com.zerobase.customboard.domain.member.type.Provider;
import com.zerobase.customboard.domain.member.type.Role;
import com.zerobase.customboard.domain.member.type.Status;
import com.zerobase.customboard.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  private Status status = INACTIVE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Provider provider = LOCAL;

}

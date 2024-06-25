package com.zerobase.customboard.domain.post.service;

import static com.zerobase.customboard.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.post.dto.PostLikeDto;
import com.zerobase.customboard.domain.post.entity.Post;
import com.zerobase.customboard.domain.post.entity.PostLike;
import com.zerobase.customboard.domain.post.repository.PostLikeRepository;
import com.zerobase.customboard.domain.post.repository.PostRepository;
import com.zerobase.customboard.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;

  public Page<PostLikeDto> getPostLikes(Long memberId, Pageable pageable) {
    Page<PostLike> postLikePage = postLikeRepository.findByMemberId(memberId, pageable);

    List<PostLikeDto> postLikeList = postLikePage.getContent().stream()
        .map(item -> PostLikeDto.to(item.getPost())).toList();

    return new PageImpl<>(postLikeList, pageable, postLikePage.getTotalElements());
  }

  @Transactional
  public boolean setPostLike(Long memberId, Long postId) {
    Member member = memberRepository.findById(memberId).orElseThrow(
        () -> new CustomException(USER_NOT_FOUND));

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    Optional<PostLike> postLike = postLikeRepository.findByMemberIdAndPostId(memberId, postId);

    if (postLike.isPresent()) {
      removeInterest(postLike.get().getId());
      return false;
    }
    addInterest(member, post);
    return true;
  }

  private void addInterest(Member member, Post post) {
    postLikeRepository.save(PostLike.builder().member(member).post(post).build());
  }

  private void removeInterest(Long postId) {
    postLikeRepository.deleteById(postId);
  }
}

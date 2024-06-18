package com.zerobase.customboard.domain.post.repository;

import com.zerobase.customboard.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<PostImage,Long> {

}

package com.zinphraek.leprestigehall.domain.usersemotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikesDislikesRepository
    extends JpaRepository<CommentLikesDislikes, Long> {}

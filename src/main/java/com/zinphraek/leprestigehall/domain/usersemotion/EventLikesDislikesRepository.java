package com.zinphraek.leprestigehall.domain.usersemotion;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventLikesDislikesRepository extends JpaRepository<EventLikesDislikes, Long> {

  @Query("SELECT eld FROM EventLikesDislikes eld WHERE eld.event.id =:id")
  Optional<EventLikesDislikes> findByEventId(@Param(("id")) UUID id);
}

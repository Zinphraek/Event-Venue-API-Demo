package com.zinphraek.leprestigehall.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

  @Query(
      "SELECT e FROM Event e WHERE (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) "
          + "AND (:maxLikes IS NULL OR ARRAY_LENGTH(e.likesDislikes.usersLiked, 1) <=:maxLikes) "
          + "AND (:minLikes IS NULL OR ARRAY_LENGTH(e.likesDislikes.usersLiked, 1) >=:minLikes) "
          + "AND (:maxDislikes IS NULL OR ARRAY_LENGTH(e.likesDislikes.usersDisliked, 1) <=:maxDislikes) "
          + "AND (:minDislikes IS NULL OR ARRAY_LENGTH(e.likesDislikes.usersDisliked, 1) >=:minDislikes)")
  Page<Event> findAllAndFilter(
      @Param("title") String title,
      @Param("minLikes") Integer minLikes,
      @Param("maxLikes") Integer maxLikes,
      @Param("minDislikes") Integer minDislikes,
      @Param("maxDislikes") Integer maxDislikes,
      Pageable pageable);

  boolean existsByTitle(String title);
}

package com.zinphraek.leprestigehall.domain.comment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {

  List<EventComment> findByEventId(UUID id);

  @Query(
      "SELECT ec FROM EventComment ec WHERE ec.eventId =:id and ec.basedCommentId is null ORDER BY ec.postedDate DESC")
  List<EventComment> findFirstLevelCommentsByEventId(@Param("id") UUID id);

  List<EventComment> findByBasedCommentId(Long id);
}

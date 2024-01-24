package com.zinphraek.leprestigehall.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventMediaRepository extends JpaRepository<EventMedia, Long> {

  void deleteByEventId(UUID eventId);

  long countByMediaUrl(String mediaUrl);

  List<EventMedia> findByEventId(UUID eventId);

}

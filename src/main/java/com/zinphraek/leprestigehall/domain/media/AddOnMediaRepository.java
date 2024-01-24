package com.zinphraek.leprestigehall.domain.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddOnMediaRepository extends JpaRepository<Media, Long> {

}

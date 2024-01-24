package com.zinphraek.leprestigehall.domain.addon;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {

  @Query("SELECT a FROM AddOn a WHERE a.price>=:minItemPrice")
  Page<AddOn> findAllAndFilter(@Param("minItemPrice") Double minItemPrice, Pageable pageable);

  boolean existsByName(String name);

  Optional<AddOn> findByName(String name);
}

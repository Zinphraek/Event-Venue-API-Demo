package com.zinphraek.leprestigehall.domain.faq;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

  @Query(
      "SELECT f FROM FAQ f WHERE (:category IS NULL OR f.category LIKE LOWER(CONCAT('%', :category, '%'))) "
          + "AND (:question IS NULL OR f.question LIKE LOWER(CONCAT('%', :question, '%')))")
  Page<FAQ> findAllAndFilter(
      @Param("category") String category,
      @Param("question") String question,
      Pageable pageable);


  Optional<FAQ> findByCategory(String category);

  boolean existsByCategory(String category);

  boolean existsByQuestion(String question);
}

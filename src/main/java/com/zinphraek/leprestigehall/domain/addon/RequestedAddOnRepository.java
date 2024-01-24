package com.zinphraek.leprestigehall.domain.addon;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestedAddOnRepository extends JpaRepository<RequestedAddOn, Long> {

  List<RequestedAddOn> findByAddOnId(Long id);

  boolean existsByAddOnId(Long Id);
}

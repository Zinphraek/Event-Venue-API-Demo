package com.zinphraek.leprestigehall.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByUsername(String username);

  User findByEmail(String email);

  User findByUsernameOrEmail(String username, String email);

  Boolean existsByUsername(String username);

  Boolean existsByUserId(String userId);

  Boolean existsByEmail(String email);

  void deleteByUserId(String userId);

  Optional<User> findByUserId(String userId);
}

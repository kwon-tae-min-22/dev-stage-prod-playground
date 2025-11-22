package com.example.demo.board;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorUserRepository extends JpaRepository<OperatorUser, UUID> {
	Optional<OperatorUser> findByUsername(String username);
}

package com.nhom12.enggo_backend.repository;

import com.nhom12.enggo_backend.entity.identity.auth.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}

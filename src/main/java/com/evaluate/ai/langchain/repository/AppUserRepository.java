package com.evaluate.ai.langchain.repository;

import com.evaluate.ai.langchain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}


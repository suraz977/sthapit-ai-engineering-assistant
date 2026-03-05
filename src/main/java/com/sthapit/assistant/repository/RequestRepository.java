package com.sthapit.assistant.repository;

import com.sthapit.assistant.model.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<RequestEntity, String> {}
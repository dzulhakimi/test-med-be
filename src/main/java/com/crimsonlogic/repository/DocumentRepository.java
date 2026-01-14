package com.crimsonlogic.repository;

import com.crimsonlogic.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByHash(String hash);
}

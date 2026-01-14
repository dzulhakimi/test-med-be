package com.crimsonlogic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crimsonlogic.model.ResumeResult;

public interface ResumeResultRepository extends JpaRepository<ResumeResult, Long> {

List<ResumeResult> findAll();
}


package com.crimsonlogic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.crimsonlogic.model.LeaveApplication;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findAll();
    List<LeaveApplication> findByStatus(String status); // Example custom query
}
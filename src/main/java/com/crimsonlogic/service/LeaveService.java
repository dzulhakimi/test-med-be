package com.crimsonlogic.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import com.crimsonlogic.dto.LeaveApplicationDTO;
import com.crimsonlogic.model.LeaveApplication;

public interface LeaveService {
    LeaveApplicationDTO applyMedicalLeave(String employeeId, String leaveDates, String reason, MultipartFile file);
    LeaveApplicationDTO updateLeaveApplication(Long id, String leaveDates, String reason, MultipartFile file);
    LeaveApplicationDTO extractAndUpdateMcFields(Long applicationId);
    LeaveApplicationDTO toDTO(LeaveApplication app);
    List<LeaveApplicationDTO> getValidCertificatesForEmployees();
 
    
}
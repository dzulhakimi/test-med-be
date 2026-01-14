
package com.crimsonlogic.controller;

import java.util.HashMap;
import java.util.Map;

import com.crimsonlogic.dto.LeaveApplicationDTO;
import com.crimsonlogic.model.LeaveApplication;
import com.crimsonlogic.repository.LeaveApplicationRepository;
import com.crimsonlogic.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    @PostMapping("/apply-and-list")
    public ResponseEntity<List<LeaveApplicationDTO>> applyLeaveAndList(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("leaveDates") String leaveDates,
            @RequestParam("reason") String reason,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            leaveService.applyMedicalLeave(employeeId, leaveDates, reason, file);
            // Return all applications for this employee
            List<LeaveApplicationDTO> employeeApps = repository.findAll().stream()
                .filter(app -> app.getEmployeeId().equals(employeeId))
                .map(leaveService::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(employeeApps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private final LeaveService leaveService;
    private final LeaveApplicationRepository repository;

    public LeaveController(LeaveService leaveService, LeaveApplicationRepository repository) {
        this.leaveService = leaveService;
        this.repository = repository;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("leaveDates") String leaveDates,
            @RequestParam("reason") String reason,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            System.out.println("🔍 Apply Leave Request Received:");
            System.out.println("  employeeId: " + employeeId);
            System.out.println("  leaveDates: " + leaveDates);
            System.out.println("  reason: " + reason);
            System.out.println("  file: " + (file != null ? file.getOriginalFilename() + " (" + file.getSize() + " bytes)" : "null"));

            LeaveApplicationDTO application = leaveService.applyMedicalLeave(employeeId, leaveDates, reason, file);

            System.out.println("✅ Leave application created successfully");
            Map<String, Object> response = new HashMap<>();
            response.put("applicationId", application.getApplicationId());
            response.put("status", application.getStatus());
            response.put("confidenceScore", application.getConfidenceScore());
            response.put("systemSuggestion", application.getSystemSuggestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error applying leave: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to apply leave");
            errorResponse.put("details", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }

    @GetMapping("/all")
    public List<LeaveApplicationDTO> getAllApplications() {
        return repository.findAll().stream().map(leaveService::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/pending")
    public List<LeaveApplicationDTO> getPendingApplications() {
        return repository.findByStatus("PENDING").stream().map(application -> {
            LeaveApplicationDTO dto = new LeaveApplicationDTO();
            dto.setApplicationId(application.getId());
            dto.setEmployeeId(application.getEmployeeId());
            dto.setLeaveDates(application.getLeaveDates());
            dto.setReason(application.getReason());
            dto.setFileName(application.getFileName());
            dto.setStatus(application.getStatus());
            // Add extracted MC fields if available
            dto.setPatientName(application.getPatientName());
            dto.setDoctorName(application.getDoctorName());
            dto.setClinicName(application.getClinicName());
            dto.setClinicAddress(application.getClinicAddress());
            dto.setContactNumber(application.getContactNumber());
            dto.setRegistrationId(application.getRegistrationId());
            dto.setDateOfIssue(application.getDateOfIssue());
            dto.setMcNumber(application.getMcNumber());
            dto.setQrCodePresent(application.getQrCodePresent());
            dto.setQrCodeUrl(application.getQrCodeUrl());
            dto.setDownloadUrl("/uploads/" + application.getFileName());
            dto.setConfidenceScore(application.getConfidenceScore());
            dto.setSystemSuggestion(application.getSystemSuggestion());
            dto.setResultDetails(application.getResultDetails());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/extract-fields/{id}")
    public LeaveApplicationDTO extractFields(@PathVariable Long id) {
        return leaveService.extractAndUpdateMcFields(id);
    }

    @PostMapping("/verify/{id}")
    public LeaveApplicationDTO verifyLeave(
            @PathVariable Long id,
            @RequestParam("status") String status // APPROVED or REJECTED
    ) {
        LeaveApplication app = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave application not found"));
        app.setStatus(status);
        repository.save(app);
        return leaveService.toDTO(app);
    }

    @GetMapping(value = "/applications", produces = "application/json")
    public List<LeaveApplicationDTO> getApplications() {
            return repository.findAll().stream().map(application -> {
                LeaveApplicationDTO dto = new LeaveApplicationDTO();
                dto.setApplicationId(application.getId());
                dto.setEmployeeId(application.getEmployeeId());
                dto.setLeaveDates(application.getLeaveDates());
                dto.setReason(application.getReason());
                dto.setRemarks(application.getRemarks());
                dto.setFileName(application.getFileName());
                dto.setStatus(application.getStatus());
                dto.setPatientName(application.getPatientName());
                dto.setDoctorName(application.getDoctorName());
                dto.setClinicName(application.getClinicName());
                dto.setClinicAddress(application.getClinicAddress());
                dto.setContactNumber(application.getContactNumber());
                dto.setRegistrationId(application.getRegistrationId());
                dto.setDateOfIssue(application.getDateOfIssue());
                dto.setMcNumber(application.getMcNumber());
                dto.setQrCodePresent(application.getQrCodePresent());
                dto.setQrCodeUrl(application.getQrCodeUrl());
                dto.setDownloadUrl("/uploads/" + application.getFileName());
                dto.setConfidenceScore(application.getConfidenceScore());
                dto.setSystemSuggestion(application.getSystemSuggestion());
                dto.setResultDetails(application.getResultDetails());
                return dto;
            }).collect(Collectors.toList());
    }
    @GetMapping("/valid-certificates")
    public ResponseEntity<List<LeaveApplicationDTO>> getValidCertificatesForEmployees() {
        List<LeaveApplicationDTO> validCertificates = leaveService.getValidCertificatesForEmployees();
        return ResponseEntity.ok(validCertificates);
    }

}
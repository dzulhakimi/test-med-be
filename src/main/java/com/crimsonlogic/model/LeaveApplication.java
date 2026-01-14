package com.crimsonlogic.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_applications")
public class LeaveApplication {
    private Integer confidenceScore;
    private String systemSuggestion;
    private String resultDetails;
    private String qrCodeUrl;

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeId;
    private String leaveDates;
    private String reason;
    private String fileName;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();

    // MC extracted fields
    private String patientName;
    private String doctorName;
    private String clinicName;
    private String clinicAddress;
    private String contactNumber;
    private String registrationId;
    private String dateOfIssue;
    private String mcNumber;
    private Boolean qrCodePresent;
    // Added remarks field
    private String remarks;
 
    // Added certificateHash field
    private String certificateHash;
    // Getters and Setters
    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getSystemSuggestion() { return systemSuggestion; }
    public void setSystemSuggestion(String systemSuggestion) { this.systemSuggestion = systemSuggestion; }
    public String getResultDetails() { return resultDetails; }
    public void setResultDetails(String resultDetails) { this.resultDetails = resultDetails; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getLeaveDates() { return leaveDates; }
    public void setLeaveDates(String leaveDates) { this.leaveDates = leaveDates; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getDateOfIssue() { return dateOfIssue; }
    public void setDateOfIssue(String dateOfIssue) { this.dateOfIssue = dateOfIssue; }

    public String getMcNumber() { return mcNumber; }
    public void setMcNumber(String mcNumber) { this.mcNumber = mcNumber; }

    public Boolean getQrCodePresent() { return qrCodePresent; }
    public void setQrCodePresent(Boolean qrCodePresent) { this.qrCodePresent = qrCodePresent; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCertificateHash() { return certificateHash; }
    public void setCertificateHash(String certificateHash) { this.certificateHash = certificateHash; }

}
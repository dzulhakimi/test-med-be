package com.crimsonlogic.dto;

public class LeaveApplicationDTO {
    private Integer confidenceScore;
    private String systemSuggestion;
    private String resultDetails;
    private Long applicationId;
    private String employeeId;
    private String leaveDates;
    private String reason;
    private String fileName;
    private String status;

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
    private String qrCodeUrl;
    private String remarks;
    private String downloadUrl;

    public LeaveApplicationDTO() {}
    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getSystemSuggestion() { return systemSuggestion; }
    public void setSystemSuggestion(String systemSuggestion) { this.systemSuggestion = systemSuggestion; }
    public String getResultDetails() { return resultDetails; }
    public void setResultDetails(String resultDetails) { this.resultDetails = resultDetails; }

    public LeaveApplicationDTO(Long applicationId, String employeeId, String leaveDates, String reason, String fileName, String status, String patientName, String doctorName, String clinicName, String clinicAddress, String contactNumber, String registrationId, String dateOfIssue, String mcNumber, Boolean qrCodePresent, String qrCodeUrl, String remarks, String downloadUrl, Integer confidenceScore, String systemSuggestion, String resultDetails) {
        this.applicationId = applicationId;
        this.employeeId = employeeId;
        this.leaveDates = leaveDates;
        this.reason = reason;
        this.fileName = fileName;
        this.status = status;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.contactNumber = contactNumber;
        this.registrationId = registrationId;
        this.dateOfIssue = dateOfIssue;
        this.mcNumber = mcNumber;
        this.qrCodePresent = qrCodePresent;
        this.qrCodeUrl = qrCodeUrl;
        this.remarks = remarks;
        this.downloadUrl = downloadUrl;
        this.confidenceScore = confidenceScore;
        this.systemSuggestion = systemSuggestion;
        this.resultDetails = resultDetails;
    }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

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
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
 
    public String getDownloadUrl() {

        return downloadUrl;

    }
 
    public void setDownloadUrl(String downloadUrl) {

        this.downloadUrl = downloadUrl;

    }

 
}
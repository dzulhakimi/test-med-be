package com.crimsonlogic.service;

import com.crimsonlogic.dto.LeaveApplicationDTO;
import com.crimsonlogic.model.LeaveApplication;
import com.crimsonlogic.repository.LeaveApplicationRepository;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.LuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.io.ByteArrayInputStream;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveApplicationRepository repository;
    private final SupabaseStorageService supabaseStorageService;

    @Value("${upload.dir:#{null}}")
    private String uploadDir;

    @Value("${supabase.storage.enabled:false}")
    private boolean supabaseStorageEnabled;

    public LeaveServiceImpl(LeaveApplicationRepository repository, SupabaseStorageService supabaseStorageService) {
        this.repository = repository;
        this.supabaseStorageService = supabaseStorageService;
    }

    private String computeSHA256Hash(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public LeaveApplicationDTO applyMedicalLeave(String employeeId, String leaveDates, String reason, MultipartFile file) {
        String extractedText = extractText(file);
        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            System.err.println("Error reading uploaded file: " + e.getMessage());
            throw new RuntimeException("Failed to read uploaded file. Please check the file and try again.");
        }
        String fileName = saveFile(file);
        String certificateHash = fileBytes != null ? computeSHA256Hash(fileBytes) : null;

        LeaveApplication application = new LeaveApplication();
        application.setEmployeeId(employeeId);
        application.setLeaveDates(leaveDates);
        application.setReason(reason);
        application.setFileName(fileName);
        application.setStatus("PENDING");
        application.setCertificateHash(certificateHash);

        List<LeaveApplication> existingApplications = repository.findAll();
        // Only check for duplicate by certificate hash
        boolean isAlreadyUsedByEmployee = existingApplications.stream()
            .anyMatch(other -> other.getCertificateHash() != null && other.getCertificateHash().equals(certificateHash));
        if (isAlreadyUsedByEmployee) {
            application.setRemarks("This certificate has already been utilized.");
        }

        extractAndSetMcFields(application, extractedText, isAlreadyUsedByEmployee);

        repository.save(application);

        return toDTO(application);
    }

    @Override
    public LeaveApplicationDTO updateLeaveApplication(Long id, String leaveDates, String reason, MultipartFile file) {
        LeaveApplication application = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave application not found"));
        application.setLeaveDates(leaveDates);
        application.setReason(reason);
        if (file != null && !file.isEmpty()) {
            String fileName = saveFile(file);
            application.setFileName(fileName);
        }
        repository.save(application);
        return toDTO(application);
    }

    @Override
    public LeaveApplicationDTO extractAndUpdateMcFields(Long applicationId) {
        LeaveApplication application = repository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Leave application not found"));
        // Always use the file from your permanent upload directory

        Path filePath = Paths.get(uploadDir, application.getFileName());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + filePath);
        }
        try (InputStream input = Files.newInputStream(filePath)) {
            String extractedText = extractText(input);
            extractAndSetMcFields(application, extractedText,false);
            repository.save(application);
            return toDTO(application);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract MC fields", e);
        }
    }

    // Returns a list of LeaveApplicationDTOs for employees with valid certificates
    @Override
    public List<LeaveApplicationDTO> getValidCertificatesForEmployees() {
        List<LeaveApplicationDTO> validList = new ArrayList<>();
        List<LeaveApplication> allApplications = repository.findAll();
        for (LeaveApplication app : allApplications) {
            // Check for duplicate certificate usage by fileName
            boolean isDuplicate = allApplications.stream()
                .filter(other -> other != app)
                .anyMatch(other -> other.getFileName() != null && other.getFileName().equals(app.getFileName()));
            if (isDuplicate) {
                app.setRemarks("This certificate has already been used by another employee.");
            }
            // Consider valid if remarks contain the success message
            if (app.getRemarks() != null && app.getRemarks().contains("Certificate matches application and contains valid doctor information, dates, file integrity, and mandatory fields.")) {
                validList.add(toDTO(app));
            }
        }
        return validList;
    }

    private String saveFile(MultipartFile file) {
        try {
            if (supabaseStorageEnabled) {
                // Use Supabase Storage
                String fileUrl = supabaseStorageService.uploadFile(file);
                // Return just the filename part for backward compatibility
                String[] parts = fileUrl.split("/");
                return parts[parts.length - 1];
            } else {
                // Use local filesystem (original implementation)
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                file.transferTo(filePath.toFile());
                return filename;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private String extractText(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            String contentType = file.getContentType();
            if (contentType != null && (contentType.startsWith("image/"))) {
                // Use OCR for image files
                return ocrImage(input);
            } else {
                // Use existing logic for PDFs
                return extractText(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String extractText(InputStream input) {
        try {
            AutoDetectParser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            parser.parse(input, handler, metadata);
            String text = handler.toString();
            if (text.trim().isEmpty()) {
                // Reset the stream for OCR
                if (input.markSupported()) {
                    input.reset();
                } else {
                    // If mark/reset not supported, re-open the stream from file
                    // You may need to pass the file path instead of InputStream for OCR fallback
                    return ""; // Or handle accordingly
                }
                text = ocrPdf(input);
            }
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void extractAndSetMcFields(LeaveApplication application, String text, boolean flag) {
    // 1. Extract all fields first
    System.out.println("=== Extracted MC Text ===");
    System.out.println(text);

    application.setPatientName(extractField(text, "Patient"));
    application.setClinicAddress(extractField(text, "Address"));
    application.setContactNumber(extractField(text, "Phone"));
    application.setRegistrationId(extractField(text, "Patient ID"));

    // Try both "Patient" and "Patient Name:" for patientName
    String patientName = extractField(text, "Patient");
    if (patientName == null) {
        patientName = extractField(text, "Patient Name");
    }
    application.setPatientName(patientName);
    application.setMcNumber(extractField(text, "Email"));
    application.setClinicAddress(extractField(text, "Address"));
    application.setContactNumber(extractField(text, "Phone"));
    application.setRegistrationId(extractField(text, "Patient ID"));
    application.setDateOfIssue(extractField(text, "Date"));
    application.setMcNumber(extractField(text, "Email"));

    // Extract clinic name: first line ending with 'Clinic' or label 'Hospital:'
    String clinicName = null;
    for (String line : text.split("\r?\n")) {
        if (line.trim().endsWith("Clinic")) {
            clinicName = line.trim();
            break;
        }
    }
    if (clinicName == null) {
        clinicName = extractField(text, "Hospital");
    }
    application.setClinicName(clinicName);

    // Extract doctor name: first line starting with 'Dr.' or containing 'Physician' or 'Examiner', or label 'Doctor’s Name & Designation:'
    String doctorName = null;
    for (String line : text.split("\r?\n")) {
        if (line.trim().startsWith("Dr.")) {
            doctorName = line.trim();
            break;
        }
        if (line.contains("Physician") || line.contains("Examiner")) {
            int idx = line.indexOf("Physician");
            if (idx == -1) idx = line.indexOf("Examiner");
            if (idx > 0) {
                doctorName = line.substring(0, idx).trim();
                break;
            }
        }
    }
    if (doctorName == null) {
        doctorName = extractField(text, "Doctor’s Name & Designation");
    }
    application.setDoctorName(doctorName);

    // QR code extraction
    String qrCodeUrl = null;
    try {
        Path filePath = Paths.get(uploadDir, application.getFileName());
        BufferedImage image = javax.imageio.ImageIO.read(filePath.toFile());
        if (image != null) {
            System.out.println("[QR] Image loaded for QR extraction: " + application.getFileName());
            qrCodeUrl = extractQrCodeUrl(image);
            System.out.println("[QR] Extracted QR code URL: " + qrCodeUrl);
        } else {
            System.out.println("[QR] Failed to load image for QR extraction: " + application.getFileName());
        }
    } catch (Exception e) {
        System.out.println("[QR] Exception during QR extraction: " + e.getMessage());
        qrCodeUrl = null;
    }
    // Debug log for qrCodeUrl
    System.out.println("[QR] Final qrCodeUrl before set: " + qrCodeUrl);
    boolean validQrUrl = false;
    if (qrCodeUrl != null && !qrCodeUrl.trim().isEmpty()) {
        try {
            new java.net.URL(qrCodeUrl);
            validQrUrl = true;
        } catch (Exception e) {
            validQrUrl = false;
        }
    }
    if (validQrUrl) {
        application.setQrCodePresent(true);
        application.setQrCodeUrl(qrCodeUrl);
    } else {
        application.setQrCodeUrl(null);
    }
    application.setQrCodePresent(validQrUrl);

    // Check text consistency: illness in certificate may or may not be present
    String extractedIllness = extractField(text, "Illness");
    boolean illnessMatches = true;
    if (extractedIllness != null) {
        illnessMatches = extractedIllness.equalsIgnoreCase(application.getReason());
    }

    // Doctor information validation
    boolean doctorNamePresent = application.getDoctorName() != null && !application.getDoctorName().trim().isEmpty();
    boolean registrationIdPresent = application.getRegistrationId() != null && !application.getRegistrationId().trim().isEmpty();

    // File integrity check
    boolean fileValid = true;
    StringBuilder fileRemarks = new StringBuilder();
    if (application.getFileName() == null || application.getFileName().trim().isEmpty()) {
        fileValid = false;
        fileRemarks.append("Certificate file is missing. ");
    } else {
        Path filePath = Paths.get(uploadDir, application.getFileName());
        try {
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                fileValid = false;
                fileRemarks.append("Certificate file is corrupted or empty. ");
            }
        } catch (Exception e) {
            fileValid = false;
            fileRemarks.append("Error checking certificate file integrity. ");
        }
    }

    // 2. Now calculate confidence score and details
    int score = 100;
    StringBuilder scoreDetails = new StringBuilder();
    // 1) Duplicate file attached
    boolean isDuplicate = flag;
    if (isDuplicate) {
        score -= 30;
        scoreDetails.append("Duplicate certificate detected. ");
        System.out.println("[CONFIDENCE] Duplicate detected, score -30 => " + score);
    }
    // 2) Mandatory field checks
    int mandatoryMissing = 0;
    if (application.getPatientName() == null || application.getPatientName().trim().isEmpty()) mandatoryMissing++;
    if (application.getClinicName() == null || application.getClinicName().trim().isEmpty()) mandatoryMissing++;
    if (application.getContactNumber() == null || application.getContactNumber().trim().isEmpty()) mandatoryMissing++;
    if (application.getDoctorName() == null || application.getDoctorName().trim().isEmpty()) mandatoryMissing++;
    if (application.getRegistrationId() == null || application.getRegistrationId().trim().isEmpty()) mandatoryMissing++;
    if (application.getDateOfIssue() == null || application.getDateOfIssue().trim().isEmpty()) mandatoryMissing++;
    if (mandatoryMissing > 0) {
        score -= mandatoryMissing * 10;
        scoreDetails.append(mandatoryMissing + " mandatory fields missing. ");
        System.out.println("[CONFIDENCE] " + mandatoryMissing + " mandatory fields missing, score -" + (mandatoryMissing*10) + " => " + score);
    }
    // 3) Invalid certificate
    boolean invalidCert = false;
    if (application.getRemarks() != null && application.getRemarks().toLowerCase().contains("invalid")) {
        invalidCert = true;
        score -= 20;
        scoreDetails.append("Certificate marked invalid. ");
        System.out.println("[CONFIDENCE] Certificate marked invalid, score -20 => " + score);
    }
    // 4) Certificate already used
    boolean alreadyUsed = false;
    if (application.getRemarks() != null && application.getRemarks().toLowerCase().contains("already been used")) {
        alreadyUsed = true;
        score -= 50;
        scoreDetails.append("Certificate already used. ");
        System.out.println("[CONFIDENCE] Certificate already used, score -50 => " + score);
    }
    // 5) QR code present and valid
    boolean qrPresent = application.getQrCodePresent() != null && application.getQrCodePresent();
    boolean qrValid = application.getQrCodeUrl() != null && !application.getQrCodeUrl().trim().isEmpty();
    if (!qrPresent || !qrValid) {
        score -= 10;
        scoreDetails.append("QR code missing or invalid. ");
        System.out.println("[CONFIDENCE] QR code missing or invalid, score -10 => " + score);
    }
    // Clamp score between 0 and 100
    if (score < 0) score = 0;
    if (score > 100) score = 100;
    System.out.println("[CONFIDENCE] Final score: " + score);
    application.setConfidenceScore(score);
    application.setResultDetails(scoreDetails.toString().trim());
    if (score >= 80) {
        application.setSystemSuggestion("Approve");
        System.out.println("[CONFIDENCE] System Suggestion: Approve");
    } else {
        application.setSystemSuggestion("Reject");
        System.out.println("[CONFIDENCE] System Suggestion: Reject");
    }

    // Remarks logic remains unchanged
    boolean patientNamePresent = application.getPatientName() != null && !application.getPatientName().trim().isEmpty();
    boolean clinicNamePresent = application.getClinicName() != null && !application.getClinicName().trim().isEmpty();
    boolean contactDetailsPresent = application.getContactNumber() != null && !application.getContactNumber().trim().isEmpty();
    int missingCount = 0;
    if (!patientNamePresent) missingCount++;
    if (!clinicNamePresent) missingCount++;
    if (!contactDetailsPresent) missingCount++;
    if (!doctorNamePresent) missingCount++;
    if (!registrationIdPresent) missingCount++;
    if (!fileValid) missingCount++;
    StringBuilder remarks = new StringBuilder();
    if (missingCount >= 5) {
        remarks.append("Certificate is invalid. Most required information is missing.");
    } else {
        // Failure points
        if (!patientNamePresent) remarks.append("Patient name is missing. ");
        if (!clinicNamePresent) remarks.append("Clinic name is missing. ");
        if (!contactDetailsPresent) remarks.append("Contact details are missing. ");
        if (extractedIllness != null && !illnessMatches) remarks.append("Illness in certificate does not match application. ");
        if (!doctorNamePresent) remarks.append("Doctor name missing in certificate. ");
        if (!registrationIdPresent) remarks.append("Doctor registration number missing in certificate. ");
        if (!fileValid) remarks.append(fileRemarks);

        // Success/valid points
        if (!isDuplicate) remarks.append("No duplicate certificate detected. ");
        if (mandatoryMissing == 0 && qrPresent && qrValid) {
            remarks.append("All mandatory fields including QR code are present. ");
        } else {
            remarks.append("Missing mandatory fields: ");
            if (!patientNamePresent) remarks.append("Patient Name, ");
            if (!clinicNamePresent) remarks.append("Clinic Name, ");
            if (!contactDetailsPresent) remarks.append("Contact Number, ");
            if (!doctorNamePresent) remarks.append("Doctor Name, ");
            if (!registrationIdPresent) remarks.append("Registration ID, ");
            if (application.getDateOfIssue() == null || application.getDateOfIssue().trim().isEmpty()) remarks.append("Date Of Issue, ");
            if (!qrPresent || !qrValid) remarks.append("QR Code, ");
            String missingFields = remarks.toString();
            if (missingFields.endsWith(", ")) {
                remarks.setLength(remarks.length() - 2); // Remove trailing comma
            }
            remarks.append(". ");
        }
        if (application.getRemarks() == null || !application.getRemarks().toLowerCase().contains("invalid")) remarks.append("Certificate is not marked invalid. ");
        if (application.getRemarks() == null || !application.getRemarks().toLowerCase().contains("already been used")) remarks.append("Certificate is not used by anyone else. ");
        if (qrPresent && qrValid) {
            remarks.append("QR code is present with valid URL. ");
        } else {
            remarks.append("QR code is missing or invalid. ");
        }
    }
    if (flag) {
        if (application.getRemarks() != null && !application.getRemarks().isEmpty()) {
            application.setRemarks(application.getRemarks());
        } else {
            application.setRemarks("This certificate has already been utilized by this employee or another employee.");
        }
    } else {
        application.setRemarks(remarks.toString().trim());
    }
    }

    private String extractField(String text, String label) {
        // Match label with any whitespace between words, then any whitespace before colon, then value
        String regex = label.replace(" ", "\\s*") + "\\s*:\\s*([^\\n]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String ocrPdf(InputStream pdfInputStream) {
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/Tesseract-OCR/tessdata"); // Set path to tessdata
            StringBuilder sb = new StringBuilder();
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                String result = tesseract.doOCR(bim);
                sb.append(result).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String ocrImage(InputStream imageInputStream) {
        try {
            BufferedImage image = javax.imageio.ImageIO.read(imageInputStream);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/Tesseract-OCR/tessdata"); // Set your tessdata path
            return tesseract.doOCR(image);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public LeaveApplicationDTO toDTO(LeaveApplication app) {
    LeaveApplicationDTO dto = new LeaveApplicationDTO();
    dto.setApplicationId(app.getId());
    dto.setEmployeeId(app.getEmployeeId());
    dto.setLeaveDates(app.getLeaveDates());
    dto.setReason(app.getReason());
    dto.setFileName(app.getFileName());
    dto.setStatus(app.getStatus());
    dto.setPatientName(app.getPatientName());
    dto.setDoctorName(app.getDoctorName());
    dto.setClinicName(app.getClinicName());
    dto.setClinicAddress(app.getClinicAddress());
    dto.setContactNumber(app.getContactNumber());
    dto.setRegistrationId(app.getRegistrationId());
    dto.setDateOfIssue(app.getDateOfIssue());
    dto.setMcNumber(app.getMcNumber());
    dto.setQrCodePresent(app.getQrCodePresent());
    dto.setQrCodeUrl(app.getQrCodeUrl());
    dto.setRemarks(app.getRemarks());
    dto.setConfidenceScore(app.getConfidenceScore());
    dto.setSystemSuggestion(app.getSystemSuggestion());
    dto.setResultDetails(app.getResultDetails());
    return dto;
    }

    public String extractQrCodeUrl(BufferedImage image) {
        try {
            LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            // Try to detect multiple barcodes (including QR codes) in the image
            com.google.zxing.multi.GenericMultipleBarcodeReader multiReader = new com.google.zxing.multi.GenericMultipleBarcodeReader(new MultiFormatReader());
            com.google.zxing.Result[] results = multiReader.decodeMultiple(bitmap);
            if (results != null && results.length > 0) {
                for (com.google.zxing.Result result : results) {
                    if (result.getBarcodeFormat() == com.google.zxing.BarcodeFormat.QR_CODE) {
                        System.out.println("[QR] Found QR code: " + result.getText());
                        return result.getText();
                    }
                }
                // If no QR code, return first barcode text
                return results[0].getText();
            }
            // Fallback: try single decode
            com.google.zxing.Result result = new MultiFormatReader().decode(bitmap);
            if (result != null && result.getBarcodeFormat() == com.google.zxing.BarcodeFormat.QR_CODE) {
                System.out.println("[QR] Found QR code (single): " + result.getText());
                return result.getText();
            }
            return null;
        } catch (Exception e) {
            System.out.println("[QR] Exception during QR extraction: ");
            e.printStackTrace();
            return null;
        }
    }
}

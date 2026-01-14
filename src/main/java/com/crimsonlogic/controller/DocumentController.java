package com.crimsonlogic.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.crimsonlogic.model.Document;
import com.crimsonlogic.repository.DocumentRepository;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    private final String uploadDir = "uploads/";
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("type") String type,
            @RequestParam("agree") boolean agree) {

        try {
            // Step 1: Calculate file hash
            String fileHash = calculateFileHash(file);

            // Step 2: Check for duplicate
            if (documentRepository.findByHash(fileHash).isPresent()) {
                return ResponseEntity.badRequest().body("Duplicate file detected.");
            }

            // Step 3: Save file locally
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir);
            Files.createDirectories(path);
            Files.copy(file.getInputStream(), path.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);

            // Step 4: Save to DB
            Document doc = new Document();
            doc.setName(name);
            doc.setEmail(email);
            doc.setDob(date);
            doc.setType(type);
            doc.setAgree(agree);
            doc.setFileName(uniqueFileName);
            doc.setFilePath(path.resolve(uniqueFileName).toString());
            doc.setUploadedAt(LocalDateTime.now());
            doc.setHash(fileHash); // save hash

            documentRepository.save(doc);

            return ResponseEntity.ok("Document uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    private String calculateFileHash(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    @GetMapping("/all")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadDocument(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("D:/Learning/Hackthon/MC-uploads").resolve(fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .body(Files.readAllBytes(filePath));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Could not download file: " + e.getMessage());
        }
    }

}
package com.crimsonlogic.service;

import org.springframework.web.multipart.MultipartFile;

import com.crimsonlogic.dto.ResumeResultDTO;

public interface ResumeService {
    ResumeResultDTO processResume(MultipartFile resume, String jobDescription) throws Exception;
}

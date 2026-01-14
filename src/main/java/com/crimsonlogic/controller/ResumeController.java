package com.crimsonlogic.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.crimsonlogic.dto.ResumeResultDTO;
import com.crimsonlogic.model.ResumeResult;
import com.crimsonlogic.repository.ResumeResultRepository;
import com.crimsonlogic.service.ResumeService;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeResultRepository repository;

    // ✅ Constructor with both dependencies
    public ResumeController(ResumeService resumeService, ResumeResultRepository repository) {
        this.resumeService = resumeService;
        this.repository = repository;
    }

    @PostMapping("/upload")
    public ResumeResultDTO uploadResume(
        @RequestParam("resume") MultipartFile resume,
        @RequestParam("jobDescription") String jobDescription
    ) throws Exception {
        return resumeService.processResume(resume, jobDescription);
    }

    @GetMapping("/all")
    public List<ResumeResultDTO> getAllResumes() {
        List<ResumeResult> results = repository.findAll();

        // Sort by score (highest first)
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<ResumeResultDTO> dtoList = new java.util.ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            ResumeResult result = results.get(i);
            ResumeResultDTO dto = new ResumeResultDTO();
            dto.setCandidateId(result.getCandidateId());
            dto.setScore(result.getScore());

            // Safely handle keyword suggestions
            String keywords = result.getKeywordSuggestions();
            if (keywords != null && !keywords.isEmpty()) {
                dto.setSuggestedKeywords(Arrays.asList(keywords.split(",\\s*")));
            } else {
                dto.setSuggestedKeywords(new java.util.ArrayList<>());
            }

            dto.setRank(i + 1); // ✅ Set rank based on sorted position
            dtoList.add(dto);
        }

        return dtoList;
    }    
}

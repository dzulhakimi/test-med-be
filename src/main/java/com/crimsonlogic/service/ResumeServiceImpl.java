package com.crimsonlogic.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import com.crimsonlogic.dto.ResumeResultDTO;
import com.crimsonlogic.model.ResumeResult;
import com.crimsonlogic.repository.ResumeResultRepository;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeResultRepository repository;

    @Value("${upload.dir}")
    private String uploadDir;

    public ResumeServiceImpl(ResumeResultRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResumeResultDTO processResume(MultipartFile resume, String jobDescription) {
        String resumeText = extractText(resume);
        double score = computeTFIDFSimilarity(resumeText, jobDescription);
        List<String> suggestions = suggestMissingKeywords(resumeText, jobDescription);

        ResumeResult result = new ResumeResult();
        result.setCandidateId(UUID.randomUUID().toString());
        result.setName(resume.getOriginalFilename());
        result.setScore(score);
        result.setKeywordSuggestions(String.join(", ", suggestions));
        result.setResumePath(saveFile(resume));

        repository.save(result);

        ResumeResultDTO dto = buildResumeResultDTO(result, suggestions, 1); // Rank = 1 for single
        return dto;
    }

    public List<ResumeResultDTO> rankAllResumes(List<MultipartFile> resumes, String jdText) {
        List<ResumeResultDTO> results = new ArrayList<>();

        for (MultipartFile resume : resumes) {
            String resumeText = extractText(resume);
            double score = computeTFIDFSimilarity(resumeText, jdText);
            List<String> suggestions = suggestMissingKeywords(resumeText, jdText);

            ResumeResult result = new ResumeResult();
            result.setCandidateId(UUID.randomUUID().toString());
            result.setName(resume.getOriginalFilename());
            result.setScore(score);
            result.setKeywordSuggestions(String.join(", ", suggestions));
            result.setResumePath(saveFile(resume));

            repository.save(result);

            results.add(buildResumeResultDTO(result, suggestions, 0)); // rank is assigned below
        }

        // Sort by score and assign ranks
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        return results;
    }

    private String extractText(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            AutoDetectParser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            parser.parse(input, handler, metadata);
            return handler.toString();
        } catch (Exception e) {
            throw new RuntimeException("Text extraction failed", e);
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private double computeTFIDFSimilarity(String text1, String text2) {
        List<String> allTerms = new ArrayList<>();
        Map<String, Integer> tf1 = getTermFrequency(text1, allTerms);
        Map<String, Integer> tf2 = getTermFrequency(text2, allTerms);

        double[] vec1 = allTerms.stream().mapToDouble(t -> tf1.getOrDefault(t, 0)).toArray();
        double[] vec2 = allTerms.stream().mapToDouble(t -> tf2.getOrDefault(t, 0)).toArray();

        return cosineSimilarity(vec1, vec2);
    }

    private Map<String, Integer> getTermFrequency(String text, List<String> globalTerms) {
        String[] tokens = text.toLowerCase().split("\\W+");
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0) + 1);
            if (!globalTerms.contains(token)) globalTerms.add(token);
        }
        return tf;
    }

    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        return (norm1 == 0 || norm2 == 0) ? 0 : dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private List<String> suggestMissingKeywords(String resumeText, String jdText) {
        List<String> jdWords = Arrays.asList(jdText.toLowerCase().split("\\W+"));
        List<String> resumeWords = Arrays.asList(resumeText.toLowerCase().split("\\W+"));
        return jdWords.stream()
                .filter(w -> !resumeWords.contains(w))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    private ResumeResultDTO buildResumeResultDTO(ResumeResult result, List<String> suggestions, int rank) {
        ResumeResultDTO dto = new ResumeResultDTO();
        dto.setCandidateId(result.getCandidateId());
        dto.setScore(result.getScore());
        dto.setSuggestedKeywords(suggestions);
        dto.setRank(rank);
        return dto;
    }
}

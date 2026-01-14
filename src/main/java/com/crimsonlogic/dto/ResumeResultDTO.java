package com.crimsonlogic.dto;

import java.util.List;

public class ResumeResultDTO {
    private String candidateId;
    private double score;
    private int rank;
    private List<String> suggestedKeywords;




    public ResumeResultDTO() {}

    public ResumeResultDTO(String candidateId, double score, int rank, List<String> suggestedKeywords) {
        this.candidateId = candidateId;
        this.score = score;
        this.rank = rank;
        this.suggestedKeywords = suggestedKeywords;
    }

    // Getters and Setters
    public String getCandidateId() { return candidateId; }

    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public double getScore() { return score; }

    public void setScore(double score) { this.score = score; }

    public int getRank() { return rank; }

    public void setRank(int rank) { this.rank = rank; }

    public List<String> getSuggestedKeywords() { return suggestedKeywords; }

    public void setSuggestedKeywords(List<String> suggestedKeywords) { this.suggestedKeywords = suggestedKeywords; }
}

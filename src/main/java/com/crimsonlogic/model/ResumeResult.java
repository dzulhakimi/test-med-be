package com.crimsonlogic.model;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "resume_results")
public class ResumeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateId;
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCandidateId() {
		return candidateId;
	}
	public void setCandidateId(String candidateId) {
		this.candidateId = candidateId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getKeywordSuggestions() {
		return keywordSuggestions;
	}
	public void setKeywordSuggestions(String keywordSuggestions) {
		this.keywordSuggestions = keywordSuggestions;
	}
	public String getResumePath() {
		return resumePath;
	}
	public void setResumePath(String resumePath) {
		this.resumePath = resumePath;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	private String name;
    private double score;

    @Column(length = 2000)
    private String keywordSuggestions;

    private String resumePath;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
}

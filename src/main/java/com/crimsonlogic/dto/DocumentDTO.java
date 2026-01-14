package com.crimsonlogic.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class DocumentDTO {

    private String name;
    private String email;
    private LocalDate dob;
    private String type;
    private boolean agree;
    private MultipartFile file;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocalDate getDob() {
		return dob;
	}
	public void setDob(LocalDate dob) {
		this.dob = dob;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isAgree() {
		return agree;
	}
	public void setAgree(boolean agree) {
		this.agree = agree;
	}
	public MultipartFile getFile() {
		return file;
	}
	public void setFile(MultipartFile file) {
		this.file = file;
	}

    // Getters and Setters
}

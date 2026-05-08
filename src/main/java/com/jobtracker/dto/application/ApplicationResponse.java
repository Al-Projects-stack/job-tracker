package com.jobtracker.dto.application;

import com.jobtracker.entity.JobApplication;
import com.jobtracker.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;
    private String company;
    private String jobTitle;
    private String jobUrl;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private LocalDate followUpDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApplicationResponse from(JobApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        r.id = app.getId();
        r.company = app.getCompany();
        r.jobTitle = app.getJobTitle();
        r.jobUrl = app.getJobUrl();
        r.status = app.getStatus();
        r.appliedDate = app.getAppliedDate();
        r.followUpDate = app.getFollowUpDate();
        r.notes = app.getNotes();
        r.createdAt = app.getCreatedAt();
        r.updatedAt = app.getUpdatedAt();
        return r;
    }
}

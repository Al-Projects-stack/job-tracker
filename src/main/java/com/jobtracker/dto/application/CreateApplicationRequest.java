package com.jobtracker.dto.application;

import com.jobtracker.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateApplicationRequest {

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String jobUrl;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @NotNull(message = "Applied date is required")
    private LocalDate appliedDate;

    private LocalDate followUpDate;

    private String notes;
}

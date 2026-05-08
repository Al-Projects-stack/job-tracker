package com.jobtracker.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateApplicationRequest {

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String jobUrl;

    @NotNull(message = "Applied date is required")
    private LocalDate appliedDate;

    private LocalDate followUpDate;

    private String notes;
}

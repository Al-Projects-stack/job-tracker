package com.jobtracker.service;

import com.jobtracker.dto.application.*;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.StatusHistory;
import com.jobtracker.entity.User;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.JobApplicationSpecifications;
import com.jobtracker.repository.StatusHistoryRepository;
import com.jobtracker.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public Page<ApplicationResponse> getApplications(
            String email, ApplicationStatus status, LocalDate startDate,
            LocalDate endDate, String search, int page, int size, String sortBy, String sortDir) {

        User user = userDetailsService.loadUserEntityByEmail(email);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<JobApplication> spec = Specification
                .where(JobApplicationSpecifications.forUser(user.getId()))
                .and(JobApplicationSpecifications.hasStatus(status))
                .and(JobApplicationSpecifications.appliedOnOrAfter(startDate))
                .and(JobApplicationSpecifications.appliedOnOrBefore(endDate))
                .and(JobApplicationSpecifications.matchesSearch(search));

        return applicationRepository.findAll(spec, pageable).map(ApplicationResponse::from);
    }

    public ApplicationResponse getApplication(String email, Long id) {
        User user = userDetailsService.loadUserEntityByEmail(email);
        return ApplicationResponse.from(findOwned(id, user.getId()));
    }

    @Transactional
    public ApplicationResponse createApplication(String email, CreateApplicationRequest request) {
        User user = userDetailsService.loadUserEntityByEmail(email);

        JobApplication app = applicationRepository.save(JobApplication.builder()
                .user(user)
                .company(request.getCompany())
                .jobTitle(request.getJobTitle())
                .jobUrl(request.getJobUrl())
                .status(request.getStatus())
                .appliedDate(request.getAppliedDate())
                .followUpDate(request.getFollowUpDate())
                .notes(request.getNotes())
                .build());

        logStatusChange(app, null, app.getStatus());
        return ApplicationResponse.from(app);
    }

    @Transactional
    public ApplicationResponse updateApplication(String email, Long id, UpdateApplicationRequest request) {
        User user = userDetailsService.loadUserEntityByEmail(email);
        JobApplication app = findOwned(id, user.getId());

        app.setCompany(request.getCompany());
        app.setJobTitle(request.getJobTitle());
        app.setJobUrl(request.getJobUrl());
        app.setAppliedDate(request.getAppliedDate());
        app.setFollowUpDate(request.getFollowUpDate());
        app.setNotes(request.getNotes());

        return ApplicationResponse.from(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse updateStatus(String email, Long id, UpdateStatusRequest request) {
        User user = userDetailsService.loadUserEntityByEmail(email);
        JobApplication app = findOwned(id, user.getId());

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(request.getStatus());
        app = applicationRepository.save(app);

        logStatusChange(app, oldStatus, request.getStatus());
        return ApplicationResponse.from(app);
    }

    @Transactional
    public void deleteApplication(String email, Long id) {
        User user = userDetailsService.loadUserEntityByEmail(email);
        applicationRepository.delete(findOwned(id, user.getId()));
    }

    private JobApplication findOwned(Long id, Long userId) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    private void logStatusChange(JobApplication app, ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        statusHistoryRepository.save(StatusHistory.builder()
                .application(app)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build());
    }
}

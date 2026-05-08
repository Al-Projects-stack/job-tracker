package com.jobtracker.service;

import com.jobtracker.dto.stats.StatsSummaryResponse;
import com.jobtracker.entity.User;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JobApplicationRepository applicationRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public StatsSummaryResponse getSummary(String email) {
        User user = userDetailsService.loadUserEntityByEmail(email);
        Long userId = user.getId();

        long total      = applicationRepository.countByUserId(userId);
        long applied    = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED);
        long screening  = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.SCREENING);
        long interview  = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEW);
        long technical  = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.TECHNICAL);
        long offer      = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFER);
        long rejected   = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED);
        long withdrawn  = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.WITHDRAWN);

        long responded = screening + interview + technical + offer + rejected;

        double responseRate = total > 0 ? round((double) responded / total * 100) : 0;
        double interviewConversionRate = responded > 0 ? round((double) (interview + technical) / responded * 100) : 0;
        double offerRate = total > 0 ? round((double) offer / total * 100) : 0;

        return StatsSummaryResponse.builder()
                .totalApplications(total)
                .appliedCount(applied)
                .screeningCount(screening)
                .interviewCount(interview)
                .technicalCount(technical)
                .offerCount(offer)
                .rejectedCount(rejected)
                .withdrawnCount(withdrawn)
                .responseRate(responseRate)
                .interviewConversionRate(interviewConversionRate)
                .offerRate(offerRate)
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package com.jobtracker.dto.stats;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsSummaryResponse {
    private long totalApplications;
    private long appliedCount;
    private long screeningCount;
    private long interviewCount;
    private long technicalCount;
    private long offerCount;
    private long rejectedCount;
    private long withdrawnCount;
    // percentage of applications that received any response (moved past APPLIED)
    private double responseRate;
    // percentage of responded applications that reached interview/technical stage
    private double interviewConversionRate;
    // percentage of all applications that resulted in an offer
    private double offerRate;
}

package com.jobtracker.repository;

import com.jobtracker.entity.JobApplication;
import com.jobtracker.enums.ApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class JobApplicationSpecifications {

    private JobApplicationSpecifications() {}

    public static Specification<JobApplication> forUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<JobApplication> hasStatus(ApplicationStatus status) {
        if (status == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> appliedOnOrAfter(LocalDate startDate) {
        if (startDate == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("appliedDate"), startDate);
    }

    public static Specification<JobApplication> appliedOnOrBefore(LocalDate endDate) {
        if (endDate == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("appliedDate"), endDate);
    }

    public static Specification<JobApplication> matchesSearch(String search) {
        if (search == null || search.isBlank()) return (root, query, cb) -> cb.conjunction();
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("company")), pattern),
                cb.like(cb.lower(root.get("jobTitle")), pattern)
        );
    }
}

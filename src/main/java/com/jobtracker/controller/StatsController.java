package com.jobtracker.controller;

import com.jobtracker.dto.stats.StatsSummaryResponse;
import com.jobtracker.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(statsService.getSummary(userDetails.getUsername()));
    }
}

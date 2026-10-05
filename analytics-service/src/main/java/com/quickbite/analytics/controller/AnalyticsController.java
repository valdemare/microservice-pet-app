package com.quickbite.analytics.controller;

import com.quickbite.analytics.dto.AnalyticsSummaryDto;
import com.quickbite.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        log.info("Запрос на получение сводной аналитики по заказам");
        AnalyticsSummaryDto summary = analyticsService.getSummaryDto();
        return ResponseEntity.ok(summary);
    }
}
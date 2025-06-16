package dev.javajunior.techstars_jobs_scraper.controller;

import dev.javajunior.techstars_jobs_scraper.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobScraperController {
    private final JobScraperService jobScraperService;

    @PostMapping("/scrape")
    public ResponseEntity<String> scrapeJobs(@RequestParam String jobFunction) {
        try {
            jobScraperService.scrapeJobsByFunction(jobFunction);
            return ResponseEntity.ok("Job scraping completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during job scraping: " + e.getMessage());
        }
    }
} 
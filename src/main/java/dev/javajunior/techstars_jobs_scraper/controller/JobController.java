package dev.javajunior.techstars_jobs_scraper.controller;

import dev.javajunior.techstars_jobs_scraper.dto.JobFilter;
import dev.javajunior.techstars_jobs_scraper.dto.JobResponse;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 10) Pageable pageable) {
        
        JobFilter filter = new JobFilter(location, sortBy, sortDirection);
        Page<Job> jobsPage = jobService.getJobs(filter, pageable);
        
        List<JobResponse> jobResponses = jobsPage.getContent().stream()
                .map(JobResponse::fromJob)
                .collect(Collectors.toList());
        
        Page<JobResponse> responsePage = new PageImpl<>(
                jobResponses,
                pageable,
                jobsPage.getTotalElements()
        );
        
        return ResponseEntity.ok(responsePage);
    }
} 
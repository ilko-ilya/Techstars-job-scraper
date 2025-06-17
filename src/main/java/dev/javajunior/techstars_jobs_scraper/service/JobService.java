package dev.javajunior.techstars_jobs_scraper.service;

import dev.javajunior.techstars_jobs_scraper.dto.JobFilter;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public Page<Job> getJobs(JobFilter filter, Pageable pageable) {
        return jobRepository.findByLocation(filter.location(), pageable);
    }

    public List<Job> getJobsForExport(String location, int limit) {
        return jobRepository.findByLocation(location, limit);
    }
} 
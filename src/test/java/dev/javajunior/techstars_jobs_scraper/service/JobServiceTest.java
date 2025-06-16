package dev.javajunior.techstars_jobs_scraper.service;

import dev.javajunior.techstars_jobs_scraper.dto.JobFilter;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JobServiceTest {
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getJobs_returnsPageOfJobs() {
        JobFilter filter = new JobFilter("NYC", null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "postedDate"));
        Page<Job> jobPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(jobRepository.findByLocation(eq("NYC"), any(Pageable.class))).thenReturn(jobPage);

        Page<Job> result = jobService.getJobs(filter, pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(jobRepository, times(1)).findByLocation(eq("NYC"), any(Pageable.class));
    }
} 
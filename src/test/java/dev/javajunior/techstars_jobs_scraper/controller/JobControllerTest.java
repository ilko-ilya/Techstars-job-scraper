package dev.javajunior.techstars_jobs_scraper.controller;

import dev.javajunior.techstars_jobs_scraper.dto.JobFilter;
import dev.javajunior.techstars_jobs_scraper.dto.JobResponse;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.model.Organization;
import dev.javajunior.techstars_jobs_scraper.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getJobs_returnsPageOfJobResponses() {
        // Given
        Organization organization = Organization.builder().id(1L).name("Test Org").build();
        Job job = Job.builder()
                .id(1L)
                .positionName("Test Job")
                .jobPageUrl("http://example.com")
                .organization(organization)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Job> jobPage = new PageImpl<>(Collections.singletonList(job), pageable, 1);

        when(jobService.getJobs(any(JobFilter.class), any(Pageable.class))).thenReturn(jobPage);

        // When
        ResponseEntity<Page<JobResponse>> response = jobController.getJobs("Test Location", "postedDate", "desc", pageable);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Test Job", response.getBody().getContent().get(0).getPositionName());
        assertEquals("Test Org", response.getBody().getContent().get(0).getOrganizationName());
    }
} 
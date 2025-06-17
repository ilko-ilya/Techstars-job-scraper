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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportJobsToCsv(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<Job> jobs = jobService.getJobsForExport(location, limit);
            byte[] csvBytes = generateCsv(jobs);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "techstars_jobs.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generateCsv(List<Job> jobs) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        
        // Write CSV header
        writer.write("Position Name,Organization,Location,Posted Date,Job Type,Experience Level,Remote,Application URL\n");
        
        // Write data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Job job : jobs) {
            String postedDate = job.getPostedDate() != null 
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(job.getPostedDate()), ZoneId.systemDefault()).format(formatter)
                : "";
            
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsvField(job.getPositionName()),
                escapeCsvField(job.getOrganization() != null ? job.getOrganization().getName() : ""),
                escapeCsvField(job.getLocation()),
                postedDate,
                escapeCsvField(job.getJobType()),
                escapeCsvField(job.getExperienceLevel()),
                job.getRemote() != null ? job.getRemote() : "",
                escapeCsvField(job.getApplicationUrl())
            ));
        }
        
        writer.flush();
        return outputStream.toByteArray();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 
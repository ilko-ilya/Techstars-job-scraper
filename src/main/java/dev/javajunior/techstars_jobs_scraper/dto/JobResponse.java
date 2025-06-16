package dev.javajunior.techstars_jobs_scraper.dto;

import dev.javajunior.techstars_jobs_scraper.model.Job;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String positionName;
    private String jobPageUrl;
    private String organizationName;
    private String organizationUrl;
    private String organizationLogo;
    private String laborFunction;
    private String location;
    private String postedDate;
    private String description;
    private Set<String> tags;
    private String jobType;
    private String experienceLevel;
    private String salary;
    private Boolean remote;
    private String applicationUrl;

    public static JobResponse fromJob(Job job) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedDate = job.getPostedDate() != null 
            ? LocalDateTime.ofInstant(Instant.ofEpochMilli(job.getPostedDate()), ZoneId.systemDefault()).format(formatter)
            : null;

        return JobResponse.builder()
                .id(job.getId())
                .positionName(job.getPositionName())
                .jobPageUrl(job.getJobPageUrl())
                .organizationName(job.getOrganization() != null ? job.getOrganization().getName() : null)
                .organizationUrl(job.getOrganization() != null ? job.getOrganization().getUrl() : null)
                .organizationLogo(job.getOrganization() != null ? job.getOrganization().getLogoUrl() : null)
                .laborFunction(job.getLaborFunction())
                .location(job.getLocation())
                .postedDate(formattedDate)
                .description(job.getDescription())
                .tags(job.getTags())
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .salary(job.getSalary())
                .remote(job.getRemote())
                .applicationUrl(job.getApplicationUrl())
                .build();
    }
} 
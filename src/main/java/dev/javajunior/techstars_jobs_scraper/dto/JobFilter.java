package dev.javajunior.techstars_jobs_scraper.dto;

public record JobFilter(
    String location,
    String sortBy,
    String sortDirection
) {
    public JobFilter {
        sortDirection = sortDirection == null ? "ASC" : sortDirection;
    }
} 
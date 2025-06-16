package dev.javajunior.techstars_jobs_scraper.repository;

import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByLaborFunction(String laborFunction);
    boolean existsByJobPageUrl(String jobPageUrl);
    boolean existsByPositionNameAndOrganization(String positionName, Organization organization);

    @Query("SELECT j.jobPageUrl FROM Job j WHERE j.jobPageUrl IN :urls")
    Set<String> findExistingJobUrls(@Param("urls") Set<String> urls);

    @Query("SELECT j FROM Job j WHERE (:location IS NULL OR j.location LIKE %:location%)")
    Page<Job> findByLocation(@Param("location") String location, Pageable pageable);
} 
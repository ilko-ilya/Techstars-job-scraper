package dev.javajunior.techstars_jobs_scraper.repository;

import dev.javajunior.techstars_jobs_scraper.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByUrl(String url);

    Optional<Organization> findByName(String name);

} 
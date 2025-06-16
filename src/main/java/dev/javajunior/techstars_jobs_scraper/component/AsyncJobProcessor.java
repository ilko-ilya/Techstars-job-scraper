package dev.javajunior.techstars_jobs_scraper.component;

import dev.javajunior.techstars_jobs_scraper.connector.JsoupWrapper;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.model.Organization;
import dev.javajunior.techstars_jobs_scraper.repository.JobRepository;
import dev.javajunior.techstars_jobs_scraper.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.io.IOException;

@Component
@Slf4j
public class AsyncJobProcessor {

    private final JobRepository jobRepository;
    private final OrganizationRepository organizationRepository;
    private final JsoupWrapper jsoupWrapper;
    private final ObjectMapper objectMapper;

    public AsyncJobProcessor(JobRepository jobRepository,
                             OrganizationRepository organizationRepository,
                             JsoupWrapper jsoupWrapper,
                             ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.organizationRepository = organizationRepository;
        this.jsoupWrapper = jsoupWrapper;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = DataIntegrityViolationException.class)
    public CompletableFuture<Job> processJobElement(Element jobElement, String jobFunction) throws IOException {
        try {
            // 1. Log the full element structure for debugging
            log.debug("Processing job element:\n{}", jobElement.outerHtml());

            // 2. Extract basic data
            String orgName = extractOrganizationName(jobElement);
            String orgUrl = extractOrganizationUrl(jobElement);
            String orgLogo = extractOrganizationLogo(jobElement);
            String jobUrl = extractJobUrl(jobElement);
            String positionName = extractPositionName(jobElement);
            String location = extractLocation(jobElement);
            String postedDateStr = extractPostedDate(jobElement);

            log.debug("Extracted details - Org: {} ({}), Position: {}, Location: {}, Date: {}",
                    orgName, orgUrl, positionName, location, postedDateStr);

            // 3. Check if job already exists
            if (jobRepository.existsByJobPageUrl(jobUrl)) {
                log.debug("Job already exists: {}", jobUrl);
                return CompletableFuture.completedFuture(null);
            }

            // 4. Create or find organization
            Organization organization = organizationRepository.findByName(orgName)
                    .orElseGet(() -> {
                        Organization newOrg = Organization.builder()
                            .name(orgName)
                            .url(orgUrl)
                            .logoUrl(orgLogo)
                            .build();
                        return organizationRepository.save(newOrg);
                    });

            // 5. Create job object
            Job job = Job.builder()
                    .positionName(positionName)
                .jobPageUrl(jobUrl)
                .organization(organization)
                .laborFunction(jobFunction)
                    .location(location)
                    .postedDate(parsePostedDate(postedDateStr).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();

            // 6. Get job details
            Document jobDoc = jsoupWrapper.connect(jobUrl);
            log.debug("Job page content:\n{}", jobDoc.html());

            // 7. Process JSON data (if available)
            processJobDetailsFromJson(job, jobDoc);
            
            // 8. Fallback to HTML if JSON didn't provide results
            if (StringUtils.isBlank(job.getDescription())) {
                processJobDetailsFromHtml(job, jobDoc);
            }

            // Validate organization URL
            if (StringUtils.isBlank(job.getOrganization().getUrl())) {
                log.warn("Empty organization URL for {}", job.getPositionName());
                return CompletableFuture.completedFuture(null);
            }

            // 9. Save the job
            Job savedJob = jobRepository.save(job);
            log.info("Saved job: {} (ID: {})", savedJob.getPositionName(), savedJob.getId());
            
            return CompletableFuture.completedFuture(savedJob);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate entry detected, skipping: {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error processing job element", e);
            throw e; // Re-throw the exception
        }
    }

    private void processJobDetailsFromJson(Job job, Document jobDoc) {
        try {
            Element nextDataScript = jobDoc.select("script#__NEXT_DATA__").first();
            if (nextDataScript == null) {
                log.debug("No __NEXT_DATA__ script found");
                return;
            }

            String jsonData = nextDataScript.html();
            JsonNode rootNode = objectMapper.readTree(jsonData);

            // Main paths to data in JSON
            JsonNode jobNode = rootNode.path("props").path("pageProps").path("job");
            if (jobNode.isMissingNode()) {
                jobNode = rootNode.path("initialState").path("jobs").path("currentJob");
            }

            if (!jobNode.isMissingNode()) {
                job.setDescription(jobNode.path("description").asText());
                job.setJobType(jobNode.path("jobType").asText());
                job.setExperienceLevel(jobNode.path("experienceLevel").asText());
                job.setSalary(jobNode.path("salary").asText());
                job.setRemote(jobNode.path("remote").asBoolean());
                job.setApplicationUrl(jobNode.path("applicationUrl").asText());

                // Process tags
                JsonNode tagsNode = jobNode.path("tags");
                if (tagsNode.isArray()) {
                    Set<String> tags = new HashSet<>();
                    tagsNode.forEach(tag -> tags.add(tag.asText()));
                    job.setTags(tags);
                }

                // Update organization logo
                JsonNode orgNode = jobNode.path("organization");
                if (!orgNode.isMissingNode() && StringUtils.isBlank(job.getOrganization().getLogoUrl())) {
                    job.getOrganization().setLogoUrl(orgNode.path("logoUrl").asText());
                    organizationRepository.save(job.getOrganization());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON data", e);
        }
    }

    private void processJobDetailsFromHtml(Job job, Document jobDoc) {
        // Description
        Element descElement = jobDoc.selectFirst(".job-description, [itemprop='description']");
        if (descElement != null) {
            job.setDescription(descElement.text());
        }

        // Job type
        Element jobTypeElement = jobDoc.selectFirst(".job-type, [data-testid='job-type']");
        if (jobTypeElement != null) {
            job.setJobType(jobTypeElement.text());
        }

        // Application URL
        Element applyElement = jobDoc.selectFirst("a[href*='apply'], .apply-button");
        if (applyElement != null) {
            job.setApplicationUrl(applyElement.attr("href"));
        }
    }

    private String extractOrganizationName(Element jobElement) {
        // First try to find by company-specific classes
        Element orgElement = jobElement.selectFirst(".company-info, .company-name, [itemprop='hiringOrganization']");
        
        // If not found, exclude elements containing job titles
        if (orgElement == null) {
            orgElement = jobElement.selectFirst("a:not(:contains('Engineer')):not(:contains('Developer')):not(:contains('Manager')):not(:contains('Lead')):not(:contains('Architect')):not(:contains('Designer')):not(:contains('Analyst')):not(:contains('Consultant')):not(:contains('Specialist'))");
        }
        
        return orgElement != null ? orgElement.text().trim() : "Unknown Company";
    }

    private String extractOrganizationUrl(Element jobElement) {
        Element orgLink = jobElement.selectFirst("a[href*='company'], .company-link");
        if (orgLink != null) {
            String url = orgLink.attr("href");
        if (StringUtils.isNotBlank(url)) {
                return url.startsWith("http") ? url : "https://jobs.techstars.com" + url;
            }
        }
        // Generate unique URL if not found
        return "https://jobs.techstars.com/company/unknown_" + UUID.randomUUID();
    }

    private String extractOrganizationLogo(Element jobElement) {
        Element logo = jobElement.selectFirst("img[src*='logo'], .company-logo");
        return logo != null ? logo.attr("src") : "";
    }

    private String extractJobUrl(Element jobElement) {
        Element link = jobElement.selectFirst("a[href*='jobs'], a[href*='apply']");
        if (link != null) {
            String url = link.attr("href");
            return url.startsWith("http") ? url : "https://jobs.techstars.com" + url;
        }
        return "";
    }

    private String extractPositionName(Element jobElement) {
        Element title = jobElement.selectFirst("[itemprop='title'], .job-title, h3");
        return title != null ? title.text() : "";
    }

    private String extractLocation(Element jobElement) {
        Element location = jobElement.selectFirst("[itemprop='jobLocation'], .location");
        return location != null ? location.text() : "Remote";
    }

    private String extractPostedDate(Element jobElement) {
        Element date = jobElement.selectFirst("[itemprop='datePosted'], .post-date");
        return date != null ? date.text() : "";
    }

    private LocalDate parsePostedDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return LocalDate.now();
        }

        Map<String, String> ukrainianMonths = new HashMap<>();
        ukrainianMonths.put("січ.", "Jan");
        ukrainianMonths.put("лют.", "Feb");
        ukrainianMonths.put("бер.", "Mar");
        ukrainianMonths.put("квіт.", "Apr");
        ukrainianMonths.put("трав.", "May");
        ukrainianMonths.put("черв.", "Jun");
        ukrainianMonths.put("лип.", "Jul");
        ukrainianMonths.put("серп.", "Aug");
        ukrainianMonths.put("вер.", "Sep");
        ukrainianMonths.put("жовт.", "Oct");
        ukrainianMonths.put("лист.", "Nov");
        ukrainianMonths.put("груд.", "Dec");

        // Try to parse relative dates first
        if (dateStr.contains("day ago") || dateStr.contains("days ago")) {
            int days = Integer.parseInt(dateStr.split("\\s+")[0]);
            return LocalDate.now().minusDays(days);
        }

        // Replace Ukrainian month names with English ones
        for (Map.Entry<String, String> entry : ukrainianMonths.entrySet()) {
            if (dateStr.contains(entry.getKey())) {
                dateStr = dateStr.replace(entry.getKey(), entry.getValue());
                break;
            }
        }

        // Try different date formats
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("MMM dd, yyyy"),
                DateTimeFormatter.ofPattern("MMM d, yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd MMM yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Continue to next formatter
            }
        }

        // If all parsing attempts fail, return current date
        log.warn("Failed to parse date: {}, using current date", dateStr);
        return LocalDate.now();
    }
}

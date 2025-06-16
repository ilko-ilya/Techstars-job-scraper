package dev.javajunior.techstars_jobs_scraper.service;

import dev.javajunior.techstars_jobs_scraper.connector.JsoupWrapper;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import dev.javajunior.techstars_jobs_scraper.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "app.scraping.base-url=https://jobs.techstars.com/jobs")
class JobScraperServiceIntegrationTest {

    @Autowired
    private JobScraperService jobScraperService;

    @Autowired
    private JobRepository jobRepository;

    @MockitoBean
    private JsoupWrapper jsoupWrapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobScraperService, "baseUrl", "https://jobs.techstars.com/jobs");
    }

    @Test
    void whenScrapingJobs_thenSaveToDatabase() throws IOException {
        // 1. Подготовка тестовых данных с полной структурой организации
        String jobListingHtml = """
        <html>
            <body>
                <div class="job-card">
                    <div class="company-info">
                        <a class="company-link" href="https://company1.com">Company 1</a>
                        <img class="company-logo" src="https://logo.com/1.png">
                    </div>
                    <div itemprop="title">Software Engineer</div>
                    <div itemprop="jobLocation">New York, NY</div>
                    <div class="posted-date">1 day ago</div>
                    <a href="https://jobs.techstars.com/apply/123">Apply</a>
                </div>
            </body>
        </html>
        """;

        String jobDetailHtml = """
        <html>
            <head>
                <script id="__NEXT_DATA__" type="application/json">
                {
                    "props": {
                        "pageProps": {
                            "job": {
                                "description": "Job description",
                                "tags": ["java"],
                                "jobType": "Full-time",
                                "experienceLevel": "Mid",
                                "salary": "$100,000",
                                "remote": false,
                                "applicationUrl": "https://apply.com/123",
                                "organization": {
                                    "name": "Company 1",
                                    "logoUrl": "https://logo.com/1.png",
                                    "website": "https://company1.com"
                                }
                            }
                        }
                    }
                }
                </script>
            </head>
        </html>
        """;

        // 2. Создание моков документов
        Document mockListingDoc = Jsoup.parse(jobListingHtml);
        Document mockJobDetailDoc = Jsoup.parse(jobDetailHtml);

        // 3. Настройка моков
        reset(jsoupWrapper);

        when(jsoupWrapper.connect(contains("jobs?q=Software+Engineer")))
                .thenReturn(mockListingDoc);

        when(jsoupWrapper.connect(contains("/apply/123")))
                .thenReturn(mockJobDetailDoc);

        // 4. Вызов тестируемого метода
        List<Job> jobs = jobScraperService.scrapeJobsByFunction("Software Engineer");

        // 5. Проверки
        assertNotNull(jobs, "Jobs list should not be null");
        assertEquals(1, jobs.size(), "Should find exactly one job");

        Job job = jobs.get(0);
        assertEquals("Software Engineer", job.getPositionName());
        assertNotNull(job.getOrganization(), "Organization should not be null");
        assertEquals("Company 1", job.getOrganization().getName());
        assertEquals("New York, NY", job.getLocation());
    }

    @Test
    void whenScrapingNonExistentFunction_thenReturnEmptyList() throws IOException {
        // Given
        String jobFunction = "NonExistentFunction";
        String emptyHtml = "<html><body><div>No jobs found</div></body></html>";
        Document mockDoc = Jsoup.parse(emptyHtml);

        // Reset any previous mock configurations
        reset(jsoupWrapper);

        // Mock JsoupWrapper for this test
        when(jsoupWrapper.connect(anyString())).thenReturn(mockDoc);

        // When
        List<Job> jobs = jobScraperService.scrapeJobsByFunction(jobFunction);

        // Then
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty(), "Jobs list should be empty for non-existent function");

        // Verify no jobs are saved in the database
        List<Job> savedJobs = jobRepository.findAll();
        assertTrue(savedJobs.isEmpty(), "No jobs should be saved for non-existent function");
    }

    private String loadTestHtml(String filename) throws IOException {
        File file = new File("src/test/resources/" + filename);
        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }
} 
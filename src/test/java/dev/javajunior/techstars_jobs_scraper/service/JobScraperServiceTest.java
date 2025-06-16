package dev.javajunior.techstars_jobs_scraper.service;

import dev.javajunior.techstars_jobs_scraper.component.AsyncJobProcessor;
import dev.javajunior.techstars_jobs_scraper.connector.JsoupWrapper;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobScraperServiceTest {

    @Mock
    private AsyncJobProcessor asyncJobProcessor;

    @Mock
    private JsoupWrapper jsoupWrapper;

    @InjectMocks
    private JobScraperService jobScraperService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobScraperService, "baseUrl", "https://jobs.techstars.com");
    }

    @Test
    void whenScrapingJobs_thenProcessJobElements() throws IOException {
        // Given
        String jobFunction = "Software Engineer";
        String testHtml = """
            <html>
                <body>
                    <div data-testid="job-card">
                        <div class="company-info">
                            <a class="company-link" href="https://company1.com">Company 1</a>
                            <img class="company-logo" src="https://logo.com/1.png">
                        </div>
                        <div itemprop="title">Software Engineer</div>
                        <div itemprop="jobLocation">New York, NY</div>
                        <div class="posted-date">1 day ago</div>
                        <a href="https://jobs.techstars.com/apply/123">Apply</a>
                    </div>
                    <div data-testid="job-card">
                        <div class="company-info">
                            <a class="company-link" href="https://company2.com">Company 2</a>
                            <img class="company-logo" src="https://logo.com/2.png">
                        </div>
                        <div itemprop="title">Senior Software Engineer</div>
                        <div itemprop="jobLocation">Remote</div>
                        <div class="posted-date">2 days ago</div>
                        <a href="https://jobs.techstars.com/apply/456">Apply</a>
                    </div>
                </body>
            </html>
            """;
        Document mockDoc = Jsoup.parse(testHtml);

        // Mock the JsoupWrapper
        when(jsoupWrapper.connect(anyString())).thenReturn(mockDoc);

        // Mock the behavior of asyncJobProcessor.processJobElement()
        when(asyncJobProcessor.processJobElement(any(Element.class), eq(jobFunction)))
            .thenReturn(CompletableFuture.completedFuture(mock(Job.class)));

        // When
        List<Job> jobs = jobScraperService.scrapeJobsByFunction(jobFunction);

        // Then
        assertNotNull(jobs, "Jobs list should not be null");
        assertFalse(jobs.isEmpty(), "Jobs list should not be empty");
        verify(asyncJobProcessor, times(2)).processJobElement(any(Element.class), eq(jobFunction));
    }

    @Test
    void whenNoJobsFound_thenReturnEmptyList() throws IOException {
        // Given
        String jobFunction = "NonExistentFunction";
        String emptyHtml = "<html><body><div>No jobs found</div></body></html>";
        Document mockDoc = Jsoup.parse(emptyHtml);

        // Mock the JsoupWrapper
        when(jsoupWrapper.connect(anyString())).thenReturn(mockDoc);

        // When
        List<Job> jobs = jobScraperService.scrapeJobsByFunction(jobFunction);

        // Then
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
        verify(asyncJobProcessor, never()).processJobElement(any(), any());
    }

    private String loadTestHtml(String filename) throws IOException {
        // Implementation to load test HTML file
        return ""; // TODO: Implement actual file loading
    }
} 
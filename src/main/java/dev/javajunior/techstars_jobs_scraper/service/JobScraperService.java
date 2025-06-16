package dev.javajunior.techstars_jobs_scraper.service;

import dev.javajunior.techstars_jobs_scraper.component.AsyncJobProcessor;
import dev.javajunior.techstars_jobs_scraper.connector.JsoupWrapper;
import dev.javajunior.techstars_jobs_scraper.exception.ScrapingException;
import dev.javajunior.techstars_jobs_scraper.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobScraperService {

    private final AsyncJobProcessor asyncJobProcessor;
    private final JsoupWrapper jsoupWrapper;

    @Value("${app.scraping.base-url}")
    private String baseUrl;

    @Transactional
    public List<Job> scrapeJobsByFunction(String jobFunction) {
        try {
            // Direct URL to the jobs page with the function filter
            String url = baseUrl + "?q=" + jobFunction.replace(" ", "+");

            log.debug("Connecting to URL: {}", url);
            Document doc = jsoupWrapper.connect(url);

            // Find all job cards
            Elements jobElements = doc.select("div[data-testid='job-card'], div.job-card, article.job-card");
            if (jobElements.isEmpty()) {
                // Try alternative selectors
                jobElements = doc.select("div[class*='job-listing'], div[class*='job-item'], article[class*='job']");
            }

            if (jobElements.isEmpty()) {
                return Collections.emptyList();
            }

            List<CompletableFuture<Job>> jobFutures = new ArrayList<>();
            for (Element jobElement : jobElements) {
                try {
                    CompletableFuture<Job> jobFuture = asyncJobProcessor.processJobElement(jobElement, jobFunction);
                    jobFutures.add(jobFuture);
                } catch (Exception e) {
                    log.error("Error processing job element: {}", e.getMessage(), e);
                }
            }

            // Wait for all jobs to be processed and collect results
            List<Job> jobs = new ArrayList<>();
            for (CompletableFuture<Job> future : jobFutures) {
                try {
                    Job job = future.get();
                    if (job != null) {
                        jobs.add(job);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error waiting for job processing: {}", e.getMessage(), e);
                }
            }

            return jobs;
        } catch (Exception e) {
            log.error("Error scraping jobs for function {}: {}", jobFunction, e.getMessage(), e);
            throw new ScrapingException("Failed to scrape jobs: " + e.getMessage(), e);
        }
    }
} 
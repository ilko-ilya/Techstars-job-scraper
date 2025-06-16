package dev.javajunior.techstars_jobs_scraper.connector;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface JsoupWrapper {
    Document connect(String url) throws IOException;
} 
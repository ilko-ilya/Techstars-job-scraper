package dev.javajunior.techstars_jobs_scraper.connector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DefaultJsoupWrapper implements JsoupWrapper {

    @Override
    public Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "en-US,en;q=0.9,uk;q=0.8")
                .header("Connection", "keep-alive")
                .header("DNT", "1")
                .referrer("https://www.google.com/")
                .ignoreHttpErrors(true)
                .get();
    }
} 
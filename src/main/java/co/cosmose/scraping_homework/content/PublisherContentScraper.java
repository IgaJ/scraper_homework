package co.cosmose.scraping_homework.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherContentScraper {

    private final PublisherContentRepository publisherContentRepository;

    @Scheduled(fixedRate = 3600000)
    public void scrapeAndSaveScheduled() {
        List<PublisherContent> contentList = scrape();
        save(contentList);
    }

    public List<PublisherContent> scrape() {
        List<PublisherContent> contents = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://connect.thairath.co.th/ws/kaikai/content/mirror").get();
            List<Element> items = document.select("item");
            for (Element item : items) {
                // no NPE validation or Optional, element.select.text() never returns a null
                PublisherContent content = mapToPublisherContent(item);
                logIfEmptyContent(content);
                contents.add(content);
            }
        } catch (IOException e) {
            log.error("Error during scraping" + e.getMessage());
        }
        return contents;
    }

    public void save(List<PublisherContent> contentList) {
        for (PublisherContent content : contentList) {
            if (!publisherContentRepository.existsByArticleUrl(content.getArticleUrl())) {
                publisherContentRepository.save(content);
                log.debug("Saved new content with articleUrl: {}", content.getArticleUrl());
            } else {
                log.error("Content with articleUrl {} already exists", content.getArticleUrl());
            }
        }
    }

    private PublisherContent mapToPublisherContent(Element element) {
        String articleUrl = element.select("link").text(); // returns empty string if null
        String title = element.select("title").text();
        String author = element.select("dc|creator").text();
        String htmlContent = element.select("description").text();
        String originalContent = element.outerHtml();
        String mainImageUrl = element.select("media|thumbnail").attr("url");

        String cleanedHtmlContent = htmlContent.replaceAll("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1", "");

        return PublisherContent.builder()
                .articleUrl(articleUrl)
                .title(title)
                .author(author)
                .htmlContent(cleanedHtmlContent)
                .originalContent(originalContent)
                .mainImageUrl(mainImageUrl)
                .build();
    }

    private void logIfEmptyContent(PublisherContent content) {
        if (content.getArticleUrl().isEmpty()) {
            log.debug("Url is missing for content with title: {}", content.getTitle());
        }
        if (content.getTitle().isEmpty()) {
            log.debug("Title is missing for articleUrl: {}", content.getArticleUrl());
        }
        if (content.getAuthor().isEmpty()) {
            log.debug("Author is missing for articleUrl: {}", content.getArticleUrl());
        }
        if (content.getHtmlContent().isEmpty()) {
            log.debug("Html content is missing for articleUrl: {}", content.getArticleUrl());
        }
        if (content.getOriginalContent().isEmpty()) {
            log.debug("Original content is missing for articleUrl: {}", content.getArticleUrl());
        }
        if (content.getMainImageUrl().isEmpty()) {
            log.debug("Image url is missing for articleUrl: {}", content.getArticleUrl());
        }
    }
}


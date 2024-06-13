package co.cosmose.scraping_homework.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherContentScraper {

    private final PublisherContentRepository publisherContentRepository;

    public void scrapeAndSave() {
        try {
            Document document = Jsoup.connect("https://connect.thairath.co.th/ws/kaikai/content/mirror").get();
            List<Element> items = document.select("item");
            List<PublisherContent> contents = new ArrayList<>();
            for (Element item : items) {
                PublisherContent content = mapToPublisherContent(item);
                contents.add(content);
            }
                for (PublisherContent content : contents) {
                    if (!publisherContentRepository.existsByArticleUrl(content.getArticleUrl())) {
                        publisherContentRepository.save(content);
                        log.debug("Saved new content with articleUrl: {}", content.getArticleUrl());
                    }
                }
            } catch(IOException e){
                log.debug("Error during scraping" + e.getMessage());
            }
        }

    private PublisherContent mapToPublisherContent(Element element) {
        String articleUrl = element.select("link").text();
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
}


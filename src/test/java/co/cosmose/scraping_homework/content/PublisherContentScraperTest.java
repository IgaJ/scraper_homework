package co.cosmose.scraping_homework.content;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublisherContentScraperTest {
    @Mock
    private PublisherContentRepository publisherContentRepository;
    @InjectMocks
    private PublisherContentScraper publisherContentScraper;

    void prepareMockElements(Element mockElement) {
        when(mockElement.select("link")).thenReturn(new Elements(new Element("link").text("http://mirror/article1")));
        when(mockElement.select("title")).thenReturn(new Elements(new Element("title").text("Title 1")));
        when(mockElement.select("dc|creator")).thenReturn(new Elements(new Element("dc|creator").text("Author 1")));
        when(mockElement.select("description")).thenReturn(new Elements(new Element("description").text("Long text 1")));
        when(mockElement.outerHtml()).thenReturn("<item>Original Content 1</item>");
        when(mockElement.select("media|thumbnail")).thenReturn(new Elements(new Element("media|thumbnail").attr("url", "http://mirror/img1.jpg")));
    }

    @Test
    void testScrape() throws IOException {
        Element mockElement = mock(Element.class);
        Elements mockElements = mock(Elements.class);
        Document mockDocument = mock(Document.class);
        Connection mockConnection = mock(Connection.class);

        prepareMockElements(mockElement);
        when(mockElements.iterator()).thenReturn(List.of(mockElement).iterator());
        when(mockDocument.select("item")).thenReturn(mockElements);
        when(mockConnection.get()).thenReturn(mockDocument);

        try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {
            jsoupMockedStatic.when(() -> Jsoup.connect(anyString()).get()).thenReturn(mockConnection);
            List<PublisherContent> contentList = publisherContentScraper.scrape();
            assertFalse(contentList.isEmpty());
            assertEquals(1, contentList.size());
        }
    }

    @Test
    void testSave() {
        PublisherContent content = PublisherContent.builder()
                .articleUrl("http://mirror/article1")
                .title("Title 1")
                .author("Author 1")
                .htmlContent("Description 1")
                .originalContent("<item>Original Content</item>")
                .mainImageUrl("http://mirror/jpg1")
                .build();

        when(publisherContentRepository.existsByArticleUrl(anyString())).thenReturn(false);
        publisherContentScraper.save(List.of(content));
        verify(publisherContentRepository, times(1)).save(any(PublisherContent.class));
    }

    @Test
    void testAlreadyExists() {
        PublisherContent content = PublisherContent.builder()
                .articleUrl("http://mirror/article1")
                .title("Title 1")
                .author("Author 1")
                .htmlContent("Description 1")
                .originalContent("<item>Original Content</item>")
                .mainImageUrl("http://mirror/jpg1")
                .build();

        when(publisherContentRepository.existsByArticleUrl(anyString())).thenReturn(true);
        publisherContentScraper.save(List.of(content));
        verify(publisherContentRepository, times(0)).save(any(PublisherContent.class));
    }

    @Test
    void testMapToPublisherContent() {
        Element mockElement = mock(Element.class);
        when(mockElement.select("link")).thenReturn(new Elements(new Element("link").text("http://mirror/article1")));
        when(mockElement.select("title")).thenReturn(new Elements(new Element("title").text("Title 1")));
        when(mockElement.select("dc|creator")).thenReturn(new Elements(new Element("dc|creator").text("Author 1")));
        when(mockElement.select("description")).thenReturn(new Elements(new Element("description").text("Long text 1")));
        when(mockElement.outerHtml()).thenReturn("<item>Original Content 1</item>");
        when(mockElement.select("media|thumbnail")).thenReturn(new Elements(new Element("media|thumbnail").attr("url", "http://mirror/img1.jpg")));

        PublisherContent content = publisherContentScraper.mapToPublisherContent(mockElement);
        assertNotNull(content);
        assertEquals("http://mirror/article1", content.getArticleUrl());
        assertEquals("Title 1", content.getTitle());
        assertEquals("Author 1", content.getAuthor());
        assertEquals("Long text 1", content.getHtmlContent());
        assertEquals("<item>Original Content 1</item>", content.getOriginalContent());
        assertEquals("http://mirror/img1.jpg", content.getMainImageUrl());
    }
}
package com.samnie.scholarlymail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// @SpringBootTest
class ScholarlymailApplicationTests {

//	@Test
//	void contextLoads() {
//	}

    @Mock
    private ArticleRepository articleRepo;

    @InjectMocks
    private ScholarlyMailService service;

    private Article sampleArticle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleArticle = new Article("1", "Sample", "https://example.com", "2025-01-01");
        sampleArticle.setId("1");
        sampleArticle.setAuthors(List.of("Sam Nie"));
        sampleArticle.setTags(List.of("AI", "ML"));
        sampleArticle.setNotes("Interesting read");
        sampleArticle.setRead(false);
    }

    // ✅ Test getAllArticles
    @Test
    void testGetAllArticles() {
        when(articleRepo.findAll()).thenReturn(List.of(sampleArticle));

        List<Article> articles = service.getAllArticles();

        assertEquals(1, articles.size());
        assertEquals("Sample", articles.get(0).getTitle());
        verify(articleRepo, times(1)).findAll();
    }

    // ✅ Test getArticles when found
    @Test
    void testGetArticlesFound() {
        when(articleRepo.findById("1")).thenReturn(Optional.of(sampleArticle));

        Optional<Article> result = service.getArticles("1");

        assertTrue(result.isPresent());
        assertEquals("Sample", result.get().getTitle());
    }

    // ✅ Test getArticles when not found
    @Test
    void testGetArticlesNotFound() {
        when(articleRepo.findById("2")).thenReturn(Optional.empty());

        Optional<Article> result = service.getArticles("2");

        assertTrue(result.isEmpty());
    }

    // ✅ Test postArticles
    @Test
    void testPostArticles() {
        when(articleRepo.save(sampleArticle)).thenReturn(sampleArticle);

        Article result = service.postArticles(sampleArticle);

        assertEquals("Sample", result.getTitle());
        verify(articleRepo, times(1)).save(sampleArticle);
    }

    // ✅ Test patchArticles success
    @Test
    void testPatchArticlesSuccess() {
        when(articleRepo.findById("1")).thenReturn(Optional.of(sampleArticle));
        when(articleRepo.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        updates.put("read", true);

        Optional<Article> result = service.patchArticles("1", updates);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertTrue(result.get().getRead());
    }

    // ✅ Test patchArticles not found
    @Test
    void testPatchArticlesNotFound() {
        when(articleRepo.findById("99")).thenReturn(Optional.empty());

        Optional<Article> result = service.patchArticles("99", Map.of("title", "Updated"));

        assertTrue(result.isEmpty());
        verify(articleRepo, never()).save(any());
    }

    // ✅ Test deleteArticles when found
    @Test
    void testDeleteArticlesFound() {
        when(articleRepo.existsById("1")).thenReturn(true);

        boolean deleted = service.deleteArticles("1");

        assertTrue(deleted);
        verify(articleRepo).deleteById("1");
    }

    // ✅ Test deleteArticles when not found
    @Test
    void testDeleteArticlesNotFound() {
        when(articleRepo.existsById("1")).thenReturn(false);

        boolean deleted = service.deleteArticles("1");

        assertFalse(deleted);
        verify(articleRepo, never()).deleteById(any());
    }

    // ✅ Test updateArticles toggle read status
    @Test
    void testUpdateArticlesSuccess() {
        sampleArticle.setRead(false);
        when(articleRepo.findById("1")).thenReturn(Optional.of(sampleArticle));
        when(articleRepo.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Article> result = service.updateArticles("1");

        assertTrue(result.isPresent());
        assertTrue(result.get().getRead()); // toggled from false to true
    }

    // ✅ Test updateArticles when not found
    @Test
    void testUpdateArticlesNotFound() {
        when(articleRepo.findById("404")).thenReturn(Optional.empty());

        Optional<Article> result = service.updateArticles("404");

        assertTrue(result.isEmpty());
        verify(articleRepo, never()).save(any());
    }

}

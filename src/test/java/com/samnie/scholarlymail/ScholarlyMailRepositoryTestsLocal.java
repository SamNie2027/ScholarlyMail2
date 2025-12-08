package com.samnie.scholarlymail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScholarlyMailRepositoryTestsLocal {

    @Mock
    ArticleRepository articleRepository;

    private Map<String, Article> store;

    @BeforeEach
    void setup() {
        store = new HashMap<>();

        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article a = invocation.getArgument(0);
            store.put(a.getId(), a);
            return a;
        });

        when(articleRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return Optional.ofNullable(store.get(id));
        });

        Mockito.lenient().when(articleRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(store.values()));
    }

    @Test
    void testInsertAndFindArticle() {
        Article article = new Article("1", "Sample Title", "https://example.com", "2025-01-01");
        article.setAuthors(List.of("Sam Nie"));
        articleRepository.save(article);

        Optional<Article> found = articleRepository.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Sample Title", found.get().getTitle());
        assertEquals("Sam Nie", found.get().getAuthors().get(0));
    }

    @Test
    void testFindAllArticles() {
        Article article1 = new Article("1", "Article 1", "https://a.com", "2025-01-01");
        Article article2 = new Article("2", "Article 2", "https://b.com", "2025-02-01");

        articleRepository.save(article1);
        articleRepository.save(article2);

        List<Article> articles = articleRepository.findAll();
        assertEquals(2, articles.size());
    }
}

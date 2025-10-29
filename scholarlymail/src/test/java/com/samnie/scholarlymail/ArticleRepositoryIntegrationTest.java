package com.samnie.scholarlymail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableMongoRepositories(basePackageClasses = ArticleRepository.class)
class ArticleRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @BeforeAll
    static void setup() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @Autowired
    private ArticleRepository articleRepository;

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

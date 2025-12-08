package com.samnie.scholarlymail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("Test")
class ScholarlyMailRepositoryTests {

    static CouchbaseContainer couchbase = new CouchbaseContainer("couchbase/server:7.2.0")
            .withBucket(new BucketDefinition("articles"));

    @Container
    static CouchbaseContainer container = couchbase;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connection-string", container::getConnectionString);
        registry.add("spring.couchbase.username", container::getUsername);
        registry.add("spring.couchbase.password", container::getPassword);
    }

    @BeforeAll
    static void setup() {
        couchbase.start();
        // Testcontainers will automatically create the bucket defined in .withBucket()
    }

    @Autowired
    ArticleRepository articleRepository;

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

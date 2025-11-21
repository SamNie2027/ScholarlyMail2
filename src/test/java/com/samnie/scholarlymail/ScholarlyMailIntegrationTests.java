package com.samnie.scholarlymail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EnableCouchbaseRepositories(basePackageClasses = ArticleRepository.class)
class ScholarlyMailIntegrationTests {

    static DockerImageName couchbaseImage = DockerImageName
            .parse("couchbase/server:community")
            .asCompatibleSubstituteFor("couchbase/server");

    @Container
    static final CouchbaseContainer couchbase =
            new CouchbaseContainer(couchbaseImage)
                    .withCredentials("admin", "password")
                    .withBucket(new BucketDefinition("articles"));


    @BeforeAll
    static void setup() {
        couchbase.start();
        // Testcontainers will automatically create the bucket defined in .withBucket()
    }

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScholarlyMailService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Article sampleArticle;

    @BeforeEach
    void setUp() {
        sampleArticle = new Article("1", "Sample Article", "https://example.com", "2025-01-01");
        sampleArticle.setAuthors(List.of("Sam Nie"));
        sampleArticle.setTags(List.of("AI", "ML"));
        sampleArticle.setNotes("Good read");
        sampleArticle.setRead(false);
    }

    // ✅ Home endpoint
    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("ScholarlyMail API is running!"));
    }

    // ✅ GET /articles
    @Test
    void testGetAllArticles() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Article"))
                .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }

    // ✅ GET /articles/{id} - found
    @Test
    void testGetArticleFound() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Sample Article"));
    }

    // ✅ GET /articles/{id} - not found
    @Test
    void testGetArticleNotFound() throws Exception {
        mockMvc.perform(get("/articles/1231"))
                .andExpect(status().isNotFound());
    }

    // ✅ POST /articles
    @Test
    void testPostArticles() throws Exception {
        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleArticle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Article"))
                .andExpect(jsonPath("$.url").value("https://example.com"));
    }

    // ✅ PATCH /articles/{id} - success
    @Test
    void testPatchArticlesSuccess() throws Exception {
        articleRepository.save(sampleArticle);
        Map<String, Object> updates = Map.of("title", "Updated Title", "read", true);

        mockMvc.perform(patch("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.read").value(true));
    }

    // ✅ PATCH /articles/{id} - not found
    @Test
    void testPatchArticlesNotFound() throws Exception {
        mockMvc.perform(patch("/articles/12341234`")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Nope"))))
                .andExpect(status().isNotFound());
    }

    // ✅ DELETE /articles/{id} - success
    @Test
    void testDeleteArticlesFound() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(delete("/articles/1"))
                .andExpect(status().isNoContent());
    }

    // ✅ DELETE /articles/{id} - not found
    @Test
    void testDeleteArticlesNotFound() throws Exception {
        mockMvc.perform(delete("/articles/404"))
                .andExpect(status().isNotFound());
    }

    // ✅ PATCH /articles/{id}/read - success
    @Test
    void testUpdateArticlesReadSuccess() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(patch("/articles/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    // ✅ PATCH /articles/{id}/read - not found
    @Test
    void testUpdateArticlesReadNotFound() throws Exception {
        mockMvc.perform(patch("/articles/1/read"))
                .andExpect(status().isNotFound());
    }

}
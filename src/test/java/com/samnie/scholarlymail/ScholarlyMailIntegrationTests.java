package com.samnie.scholarlymail;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ScholarlyMailApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@Tag("container")
@DisabledIfEnvironmentVariable(named = "JENKINS_HOME", matches = ".*")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@DisabledIfSystemProperty(named = "testcontainers.skip", matches = "true")
class ScholarlyMailIntegrationTests {

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
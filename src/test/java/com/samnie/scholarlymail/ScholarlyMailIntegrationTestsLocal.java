package com.samnie.scholarlymail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ScholarlyMailApplication.class)
@AutoConfigureMockMvc
class ScholarlyMailIntegrationTestsLocal {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleRepository articleRepository;

    private Article sampleArticle;
    private Map<String, Article> store;

    @BeforeEach
    void setUp() {
        sampleArticle = new Article("1", "Sample Article", "https://example.com", "2025-01-01");
        sampleArticle.setAuthors(List.of("Sam Nie"));
        sampleArticle.setTags(List.of("AI", "ML"));
        sampleArticle.setNotes("Good read");
        sampleArticle.setRead(false);

        store = new HashMap<>();

        // mock save
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article a = invocation.getArgument(0);
            store.put(a.getId(), a);
            return a;
        });

        // mock findById
        when(articleRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return Optional.ofNullable(store.get(id));
        });

        // mock findAll
        when(articleRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(store.values()));

        // mock existsById
        when(articleRepository.existsById(anyString())).thenAnswer(invocation -> store.containsKey(invocation.getArgument(0)));

        // Note: deleteById is a void method; controller logic queries existsById so removal is covered
    }

    // Home endpoint
    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("ScholarlyMail API is running!"));
    }

    @Test
    void testGetAllArticles() throws Exception {
        // seed
        articleRepository.save(sampleArticle);

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Article"))
                .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }

    @Test
    void testGetArticleFound() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Sample Article"));
    }

    @Test
    void testGetArticleNotFound() throws Exception {
        mockMvc.perform(get("/articles/1231"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostArticles() throws Exception {
        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleArticle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Article"))
                .andExpect(jsonPath("$.url").value("https://example.com"));
    }

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

    @Test
    void testPatchArticlesNotFound() throws Exception {
        mockMvc.perform(patch("/articles/12341234`")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Nope"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteArticlesFound() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(delete("/articles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteArticlesNotFound() throws Exception {
        mockMvc.perform(delete("/articles/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateArticlesReadSuccess() throws Exception {
        articleRepository.save(sampleArticle);

        mockMvc.perform(patch("/articles/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testUpdateArticlesReadNotFound() throws Exception {
        mockMvc.perform(patch("/articles/1/read"))
                .andExpect(status().isNotFound());
    }

}

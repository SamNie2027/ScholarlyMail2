package com.samnie.scholarlymail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScholarlyMailController.class)
@ExtendWith(MockitoExtension.class)
class ScholarlyMailControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        Mockito.when(service.getAllArticles()).thenReturn(List.of(sampleArticle));

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Article"))
                .andExpect(jsonPath("$[0].url").value("https://example.com"));
    }

    // ✅ GET /articles/{id} - found
    @Test
    void testGetArticleFound() throws Exception {
        Mockito.when(service.getArticles("1")).thenReturn(Optional.of(sampleArticle));

        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Sample Article"));
    }

    // ✅ GET /articles/{id} - not found
    @Test
    void testGetArticleNotFound() throws Exception {
        Mockito.when(service.getArticles("404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/articles/404"))
                .andExpect(status().isNotFound());
    }

    // ✅ POST /articles
    @Test
    void testPostArticles() throws Exception {
        Mockito.when(service.postArticles(any(Article.class))).thenReturn(sampleArticle);

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
        Map<String, Object> updates = Map.of("title", "Updated Title", "read", true);
        Article updatedArticle = new Article("1", "Updated Title", "https://example.com", "2025-01-01");
        updatedArticle.setRead(true);

        Mockito.when(service.patchArticles(eq("1"), any(Map.class))).thenReturn(Optional.of(updatedArticle));

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
        Mockito.when(service.patchArticles(eq("404"), any(Map.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/articles/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Nope"))))
                .andExpect(status().isNotFound());
    }

    // ✅ DELETE /articles/{id} - success
    @Test
    void testDeleteArticlesFound() throws Exception {
        Mockito.when(service.deleteArticles("1")).thenReturn(true);

        mockMvc.perform(delete("/articles/1"))
                .andExpect(status().isNoContent());
    }

    // ✅ DELETE /articles/{id} - not found
    @Test
    void testDeleteArticlesNotFound() throws Exception {
        Mockito.when(service.deleteArticles("404")).thenReturn(false);

        mockMvc.perform(delete("/articles/404"))
                .andExpect(status().isNotFound());
    }

    // ✅ PATCH /articles/{id}/read - success
    @Test
    void testUpdateArticlesReadSuccess() throws Exception {
        sampleArticle.setRead(true);
        Mockito.when(service.updateArticles("1")).thenReturn(Optional.of(sampleArticle));

        mockMvc.perform(patch("/articles/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    // ✅ PATCH /articles/{id}/read - not found
    @Test
    void testUpdateArticlesReadNotFound() throws Exception {
        Mockito.when(service.updateArticles("404")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/articles/404/read"))
                .andExpect(status().isNotFound());
    }
}

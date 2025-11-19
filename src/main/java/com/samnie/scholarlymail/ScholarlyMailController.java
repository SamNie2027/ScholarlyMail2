package com.samnie.scholarlymail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ScholarlyMailController {
    private final ScholarlyMailService service;
    Logger log = LoggerFactory.getLogger(ScholarlyMailController.class);

    public ScholarlyMailController(ScholarlyMailService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "ScholarlyMail API is running!";
    }

    @GetMapping("/articles")
    public ResponseEntity<List<Article>> getAllArticles() {
        log.info("GET /articles");
        return ResponseEntity.ok(service.getAllArticles());
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<Article> getArticles(@PathVariable String id) {
        log.info("GET /articles/{}", id);
        return service.getArticles(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/articles")
    public ResponseEntity<Article> postArticles(@RequestBody Article article) {
        log.info("POST /articles");
        Article saved = service.postArticles(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/articles/{id}")
    public ResponseEntity<Article> patchArticles(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        log.info("PATCH /articles/{}", id);
        return service.patchArticles(id, updates)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable String id) {
        log.info("DELETE /articles/{}", id);
        return service.deleteArticles(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/articles/{id}/read")
    public ResponseEntity<Article> updateArticles(@PathVariable String id) {
        log.info("PATCH /articles/{}/read", id);
        return service.updateArticles(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

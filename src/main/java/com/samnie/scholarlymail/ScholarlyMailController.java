package com.samnie.scholarlymail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ScholarlyMailController {
    private final ScholarlyMailService service;

    public ScholarlyMailController(ScholarlyMailService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "ScholarlyMail API is running!";
    }

    @GetMapping("/articles")
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(service.getAllArticles());
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<Article> getArticles(@PathVariable String id) {
        return service.getArticles(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/articles")
    public ResponseEntity<Article> postArticles(@RequestBody Article article) {
        Article saved = service.postArticles(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/articles/{id}")
    public ResponseEntity<Article> patchArticles(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        return service.patchArticles(id, updates)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable String id) {
        return service.deleteArticles(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/articles/{id}/read")
    public ResponseEntity<Article> updateArticles(@PathVariable String id) {
        return service.updateArticles(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

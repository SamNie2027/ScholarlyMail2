package com.samnie.scholarlymail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ScholarlyMailController {
    private final ScholarlyMailService service;
    private final ArticleRepository articleRepo;

    public ScholarlyMailController(ScholarlyMailService service, ArticleRepository articleRepo) {
        this.service = service;
        this.articleRepo = articleRepo;
    }

    // findAll method is predefined method in Mongo Repository
    // with this method we will all user that is saved in our database
    @GetMapping("/articles")
    public List<Article> getAllArticles() {
        return articleRepo.findAll();
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<Article> getArticles(@PathVariable String id) {
        Optional<Article> article = articleRepo.findById(id);
        if (article.isPresent()) {
            return ResponseEntity.ok(article.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/articles")
    public ResponseEntity<Article> postArticles(@RequestBody Article article) {
        Article savedArticle = articleRepo.save(article);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);
    }

    @PatchMapping("/articles/{id}")
    public ResponseEntity<Article> patchArticles(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Optional<Article> existingArticleOpt = articleRepo.findById(id);

        if (existingArticleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Article existingArticle = existingArticleOpt.get();

        // Apply partial updates (you can customize this logic)
        if (updates.containsKey("title")) {
            existingArticle.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("authors")) {
            existingArticle.setAuthors((List<String>) updates.get("authors"));
        }
        if (updates.containsKey("url")) {
            existingArticle.setUrl((String) updates.get("url"));
        }
        if (updates.containsKey("tags")) {
            existingArticle.setTags((List<String>) updates.get("tags"));
        }
        if (updates.containsKey("notes")) {
            existingArticle.setNotes((String) updates.get("notes"));
        }
        if (updates.containsKey("read")) {
            existingArticle.setRead((Boolean) updates.get("read"));
        }

        Article updatedArticle = articleRepo.save(existingArticle);
        return ResponseEntity.ok(updatedArticle);
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticles(@PathVariable String id) {
        if (!articleRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        articleRepo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PatchMapping("/articles/{id}/read")
    public ResponseEntity<Article> updateArticles(@PathVariable String id) {
        Optional<Article> existingArticleOpt = articleRepo.findById(id);
        if (existingArticleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            Article existingArticle = existingArticleOpt.get();
            existingArticleOpt.get().setRead((Boolean) !existingArticleOpt.get().getRead());
            Article updatedArticle = articleRepo.save(existingArticle);
            return ResponseEntity.ok(updatedArticle);
        }
    }
}

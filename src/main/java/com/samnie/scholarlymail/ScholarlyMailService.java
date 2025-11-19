package com.samnie.scholarlymail;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScholarlyMailService {
    private final ArticleRepository articleRepo;
    Logger log = LoggerFactory.getLogger(ScholarlyMailService.class);
    public ScholarlyMailService(ArticleRepository articleRepo) {
        this.articleRepo = articleRepo;
    }

    public List<Article> getAllArticles() {
        return articleRepo.findAll();
    }

    public Optional<Article> getArticles(String id) {
        log.info("Getting article with id: {}", id);
        return articleRepo.findById(id);
    }

    public Article postArticles(Article article) {
        log.info("Posting article {}", article);
        return articleRepo.save(article);
    }

    public Optional<Article> patchArticles(String id, Map<String, Object> updates) {
        log.info("Patching article {}", id);
        log.info("Updating article with updates {}", updates);
        Optional<Article> existingArticleOpt = articleRepo.findById(id);

        if (existingArticleOpt.isEmpty()) {
            return Optional.empty();
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
            existingArticle.setUrl(updates.get("url").toString());
        }
        if (updates.containsKey("tags")) {
            existingArticle.setTags((List<String>) updates.get("tags"));
        }
        if (updates.containsKey("notes")) {
            existingArticle.setNotes(updates.get("notes").toString());
        }
        if (updates.containsKey("read")) {
            existingArticle.setRead((boolean) updates.get("read"));
        }

        return Optional.of(articleRepo.save(existingArticle));
    }

    public boolean deleteArticles(String id) {
        log.info("Deleting article {}", id);
        if (!articleRepo.existsById(id)) {
            return false;
        }

        articleRepo.deleteById(id);
        return true; // 204 No Content
    }

    public Optional<Article> updateArticles(String id) {
        log.info("Updating article as read {}", id);
        Optional<Article> existingArticleOpt = articleRepo.findById(id);
        if (existingArticleOpt.isEmpty()) {
            return Optional.empty();
        } else {
            Article existingArticle = existingArticleOpt.get();
            existingArticleOpt.get().setRead(!existingArticleOpt.get().getRead());
            return Optional.of(articleRepo.save(existingArticle));
        }
    }
}

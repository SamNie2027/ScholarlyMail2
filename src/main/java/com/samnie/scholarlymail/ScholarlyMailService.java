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
    private static final Logger log = LoggerFactory.getLogger(ScholarlyMailService.class);

    public ScholarlyMailService(ArticleRepository articleRepo) {
        this.articleRepo = articleRepo;
    }

    // ------- GET all ---------
    public List<Article> getAllArticles() {
        log.info("Fetching all Couchbase articles");
        return articleRepo.findAll();
    }

    // ------- GET one ---------
    public Optional<Article> getArticles(String id) {
        log.info("Fetching Couchbase article with id: {}", id);
        return articleRepo.findById(id);
    }

    // ------- CREATE ---------
    public Article postArticles(Article article) {
        log.info("Saving new Couchbase article: {}", article);
        return articleRepo.save(article);
    }

    // ------- PATCH (partial update) ---------
    public Optional<Article> patchArticles(String id, Map<String, Object> updates) {
        log.info("Patching Couchbase article {} with updates {}", id, updates);

        Optional<Article> existingOpt = articleRepo.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Article article = existingOpt.get();

        if (updates.containsKey("title")) article.setTitle((String) updates.get("title"));
        if (updates.containsKey("authors")) article.setAuthors((List<String>) updates.get("authors"));
        if (updates.containsKey("url")) article.setUrl(updates.get("url").toString());
        if (updates.containsKey("tags")) article.setTags((List<String>) updates.get("tags"));
        if (updates.containsKey("notes")) article.setNotes((String) updates.get("notes"));
        if (updates.containsKey("read")) article.setRead((Boolean) updates.get("read"));

        return Optional.of(articleRepo.save(article));
    }

    // ------- DELETE ---------
    public boolean deleteArticles(String id) {
        log.info("Deleting Couchbase article {}", id);

        if (!articleRepo.existsById(id)) {
            return false;
        }

        articleRepo.deleteById(id);
        return true;
    }

    // ------- UPDATE toggle read ---------
    public Optional<Article> updateArticles(String id) {
        log.info("Toggling read status for Couchbase article {}", id);

        Optional<Article> existingOpt = articleRepo.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Article article = existingOpt.get();
        article.setRead(!article.getRead());

        return Optional.of(articleRepo.save(article));
    }
}

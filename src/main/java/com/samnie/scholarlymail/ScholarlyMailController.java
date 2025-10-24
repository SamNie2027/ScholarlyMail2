package com.samnie.scholarlymail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScholarlyMailController {
    private ScholarlyMailService service;

    @Autowired
    public ScholarlyMailController(ScholarlyMailService service) {
        this.service = service;
    }

    @GetMapping("/articles")
    public String getArticles() {
        return ""; //TODO: Implement
    }

    @GetMapping("/articles/{id}")
    public String getArticles(@PathVariable String id) {
        return ""; //TODO: Implement
    }

    @PostMapping("/articles")
    public String postArticles(@PathVariable String id) {
        return ""; //TODO: Implement
    }

    @PatchMapping("/articles/{id}")
    public String patchArticles(@PathVariable String id) {
        return "";  //TODO: Implement
    }

    @DeleteMapping("/articles/{id}")
    public String deleteArticles(@PathVariable String id) {
        return ""; //TODO: Implement
    }

}

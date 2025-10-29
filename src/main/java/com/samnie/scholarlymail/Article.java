package com.samnie.scholarlymail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("article")
public class Article {
    @Id
    private String id;
    private String title;
    private String url;
    private Boolean read = false;
    private String createdAt;
    private List<String> tags; // optional
    private String notes; // optional
    private List<String> authors; // optional

    public Article(String id, String title, String url, String createdAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
}

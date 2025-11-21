package com.samnie.scholarlymail;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.util.List;

@Document
public class Article {

    @Id
    private String id;

    @Field
    private String title;

    @Field
    private String url;

    @Field
    private Boolean read = false;

    @Field
    private String createdAt;

    @Field
    private List<String> tags;

    @Field
    private String notes;

    @Field
    private List<String> authors;

    // --- REQUIRED by Spring Data Couchbase ---
    public Article() {
    }

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

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
}

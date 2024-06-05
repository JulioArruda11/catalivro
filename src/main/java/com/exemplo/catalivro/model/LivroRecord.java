package com.exemplo.catalivro.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LivroRecord(
        long id,
        String title,
        List<Autor> authors,
        List<String> languages,
        int download_count
) {
    public String getTitle() {
        return title;
    }
    public List<Autor> getAuthors() {
        return authors;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public int getDownloadCount() {
        return download_count;
    }
}
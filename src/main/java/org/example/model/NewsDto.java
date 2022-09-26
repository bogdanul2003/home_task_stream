package org.example.model;

public class NewsDto {
    private final String title;

    private final String body;

    public NewsDto(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}

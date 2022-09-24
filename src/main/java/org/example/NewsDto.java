package org.example;

public class NewsDto {
    private final String Title;

    private final String Body;

    public NewsDto(String title, String body) {
        Title = title;
        Body = body;
    }

    public String getTitle() {
        return Title;
    }

    public String getBody() {
        return Body;
    }
}

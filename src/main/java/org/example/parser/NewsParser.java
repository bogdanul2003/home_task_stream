package org.example.parser;

import org.example.model.NewsDto;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class NewsParser {

    private static final String TITLE = "title";
    private static final String TEXT = "text";

    public static Optional<NewsDto> extractNews(String fileName, DocumentBuilder documentBuilder) throws IOException, SAXException {
        documentBuilder.reset();
        try {
            Document document = documentBuilder.parse(new File(fileName));
            document.getDocumentElement().normalize();
            return Optional.of(new NewsDto(
                    document.getDocumentElement().getElementsByTagName(TITLE).item(0).getTextContent(),
                    document.getDocumentElement().getElementsByTagName(TEXT).item(0).getTextContent()));
        } catch (SAXParseException ex) {
            System.out.println(fileName + " " + ex.getMessage());
            return Optional.of(new NewsDto("", ""));
        }
    }

}

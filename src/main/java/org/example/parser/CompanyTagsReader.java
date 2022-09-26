package org.example.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CompanyTagsReader {
    private static final String TAGS_FILE = "countries.csv";

    public Set<String> readTags() {
        Optional<URI> tagsURI = getTagsURI();
        Set<String> tags = new HashSet<>();

        if (tagsURI.isEmpty()) {
            return tags;
        }

        try (Scanner scanner = new Scanner(new File(tagsURI.get()))) {
            while (scanner.hasNextLine()) {
                String[] countries = scanner.nextLine().split(";");
                tags.addAll(List.of(countries));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return tags;
    }

    private Optional<URI> getTagsURI() {
        URL resource = getClass().getClassLoader().getResource(TAGS_FILE);
        try {
            return resource != null ? of(resource.toURI()) : empty();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

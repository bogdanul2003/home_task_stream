package org.example;

import org.example.model.NewsDto;
import org.example.processor.NewsProcessor;
import org.example.processor.XmlFilesProcessor;
import org.example.storage.CompaniesHash;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;

public class CompaniesProcessorController {

    private BlockingQueue<Optional<NewsDto>> queue = new LinkedBlockingDeque<>(ForkJoinPool.commonPool().getParallelism() * 4);
    private final NewsProcessor newsProcessor;

    private final XmlFilesProcessor xmlFilesProcessor;

    private long lastDuration;

    public CompaniesProcessorController(CompaniesHash hash) {
        this.newsProcessor = new NewsProcessor(queue, hash, false);
        this.xmlFilesProcessor = new XmlFilesProcessor(queue, newsProcessor.getNumberOfThreads());
    }

    public long getLastDuration() {
        return lastDuration / 1000000;
    }

    public Set<Integer> processNews(String folder) throws IOException {
        System.out.println("Starting news feed reader");
        xmlFilesProcessor.startNewsStreamFromFolder(folder);

        System.out.println("Starting news feed processing");
        long startTime = System.nanoTime();
        Set<Integer> result = newsProcessor.startNewsProcessing();
        long endTime = System.nanoTime();
        lastDuration = (endTime - startTime);

        return result;
    }
}

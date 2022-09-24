package org.example;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, URISyntaxException {
        if(args.length != 2)
        {
            System.out.println("Usage: app [companies_file] [news_folder]");
            return;
        }

        CompaniesHash hash = new CompaniesHash();
        hash.insertCompanies(args[0]);

        BlockingQueue<Optional<NewsDto>> queue = new LinkedBlockingDeque<>(ForkJoinPool.commonPool().getParallelism() * 4);

        NewsProcessor processor = new NewsProcessor(queue, hash, false);
        XmlNewsExtract news = new XmlNewsExtract(queue, processor.getNumberOfThreads());

        System.out.println("Starting news feed reader");
        news.startNewsStreamFromFolder(args[1]);
        System.out.println("Starting news feed processing");

        long startTime = System.nanoTime();
        var result = processor.startNewsProcessing();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Parallel found " + result.size() + " companies in " + duration/1000000);

        hash.printContentToFile();
//        System.out.println("Found " + hash.getFoundCompanies().size() + " companies in " + duration/1000000);
    }
}
package org.example.processor;

import org.example.model.NewsDto;
import org.example.parser.NewsParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class XmlFilesProcessor {
    private static final Integer parallelStreams = 6;
    public static final String MY_NEWS_EXTRACTOR = "my-news-extractor-";
    final private List<DocumentBuilderFactory> documentBuilderFactory;
    final private Map<String, DocumentBuilder> documentBuilders;

    final private ForkJoinPool customThreadPool;

    final private BlockingQueue<Optional<NewsDto>> newsQueue;

    private final ThreadPoolUsageMeter usageMeter;

    private final AtomicBoolean stopCondition = new AtomicBoolean(false);

    private int poisonPills;

    private ForkJoinTask<?> currentTask;

    public XmlFilesProcessor(BlockingQueue<Optional<NewsDto>> queue, int numberOfPills) {
        newsQueue = queue;
        documentBuilderFactory = new ArrayList<>();
        documentBuilders = new HashMap<>();
        poisonPills = numberOfPills;
        for (int i = 0; i < parallelStreams; i++) {

            try {
                var factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                documentBuilderFactory.add(factory);
                documentBuilders.put(MY_NEWS_EXTRACTOR + i, builder);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }

        }

        var factory = new CustomWorkerThreadFactory(MY_NEWS_EXTRACTOR);
        customThreadPool = new ForkJoinPool(parallelStreams, factory, null, false);
        usageMeter = new ThreadPoolUsageMeter(customThreadPool);
    }

    public void stopNewsStream() {
        stopCondition.set(true);
        customThreadPool.shutdownNow();
    }

    public void waitOnCurrentTask() {
        currentTask.join();
    }

    public void startNewsStreamFromFolder(String folder) throws IOException {
        Path folderPath = Paths.get(folder);
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Not a valid folder!");
            return;
        }

        Stream<Path> paths = Files.walk(folderPath);
        currentTask = customThreadPool.submit(() -> processNewsFiles(paths, folderPath));
    }

    private void processNewsFiles(Stream<Path> paths, Path folderPath) {

        paths.unordered()
                .parallel()
                .takeWhile(p -> !stopCondition.get())
                .forEach(filePath -> processNewsFile(filePath, folderPath));

        System.out.println("##############################################################################");
        usageMeter.printUsage();

        sendPoisonPills();
    }

    private void processNewsFile(Path filePath, Path folderPath) {
        if(filePath.equals(folderPath)){
            return;
        }

        try {
            var result = NewsParser.extractNews(filePath.toString(), documentBuilders.get(Thread.currentThread().getName()));
            usageMeter.monitorUsage();

            while (!newsQueue.offer(result, 1, TimeUnit.SECONDS)) {
                if (stopCondition.get())
                    break;
            }
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendPoisonPills() {
        try {
            while (poisonPills >= 0) {
                if (newsQueue.offer(Optional.empty(), 1, TimeUnit.SECONDS)) {
                    poisonPills--;
                }
                if (stopCondition.get())
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

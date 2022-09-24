package org.example;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class XmlNewsExtract {
    private static final Integer parallelStreams = 6;
    final private List<DocumentBuilderFactory> documentBuilderFactory;
    final private Map<String, DocumentBuilder> documentBuilders;

    final private ForkJoinPool customThreadPool;

    final private BlockingQueue<Optional<NewsDto>> newsQueue;

    private final ThreadPoolUsageMeter usageMeter;

    private AtomicBoolean stopCondition = new AtomicBoolean(false);

    private int poisonPills;

    XmlNewsExtract(BlockingQueue<Optional<NewsDto>> queue, int numberOfPills) throws ParserConfigurationException {
        newsQueue = queue;
        documentBuilderFactory = new ArrayList<>();
        documentBuilders = new HashMap<>();
        poisonPills = numberOfPills;
        for(int i=0; i< parallelStreams; i++) {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            documentBuilderFactory.add(factory);
            documentBuilders.put("my-news-extractor-" + i, builder);
        }

        var factory = new CustomWorkerThreadFactory("my-news-extractor-");
        customThreadPool = new ForkJoinPool(parallelStreams, factory, null, false);
        usageMeter = new ThreadPoolUsageMeter(customThreadPool);
    }

    public void stopNewsStream()
    {
        stopCondition.set(true);
        customThreadPool.shutdownNow();
    }

    public void startNewsStreamFromFolder(String folder) throws IOException {
        var path = Paths.get(folder);
        if(!Files.isDirectory(path)) {
            System.out.println("Not a valid folder!");
            return;
        }
        Stream<Path> paths = Files.walk(path);
        customThreadPool.submit(() -> {

           paths.unordered().parallel().takeWhile(p -> stopCondition.get() == false)
                   .forEach(p -> {
                       try {
                           var result = getNewsBlock(p.toString());
                           usageMeter.monitorUsage();

                           while(!newsQueue.offer(result, 1, TimeUnit.SECONDS)) {
                               if(stopCondition.get())
                                   break;
                           }
                       } catch (IOException | SAXException e) {
                           throw new RuntimeException(e);
                       }
                       catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }
                   });
            System.out.println("##############################################################################");
            usageMeter.printUsage();
            try {
                while(poisonPills >= 0) {
                    if(newsQueue.offer(Optional.empty(), 1, TimeUnit.SECONDS)) {
                        poisonPills--;
                    }
                    else{
                        System.out.println("Could not send poison!!!");
                    }
                    if(stopCondition.get())
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private Optional<NewsDto> getNewsBlock(String fileName) throws IOException, SAXException {
        var documentBuilder = documentBuilders.get(Thread.currentThread().getName());
        documentBuilder.reset();
        try {
            Document document = documentBuilder.parse(new File(fileName));
            document.getDocumentElement().normalize();
            var result = new NewsDto(document.getDocumentElement().getElementsByTagName("title").item(0).getTextContent(),
                    document.getDocumentElement().getElementsByTagName("text").item(0).getTextContent());
            return Optional.of(result);
        } catch (SAXParseException ex)
        {
            System.out.println(fileName + " " + ex.getMessage());
            return Optional.of(new NewsDto("", ""));
        }
    }
}

package org.example.processor;

import org.example.model.NewsDto;
import org.example.storage.CompaniesHash;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NewsProcessor {
    public static final String MY_NEWS_PROCESSOR = "my-news-processor-";
    private static Integer parallelStreams;

    final private ForkJoinPool customThreadPool;

    final private CompaniesHash companiesHash;

    final private BlockingQueue<Optional<NewsDto>> newsQueue;

    private AtomicBoolean stopCondition = new AtomicBoolean(false);

    private final ThreadPoolUsageMeter usageMeter;

    public NewsProcessor(BlockingQueue<Optional<NewsDto>> queue, CompaniesHash hash, boolean singleThreaded) {
        newsQueue = queue;
        companiesHash = hash;

        parallelStreams = singleThreaded ? 1 : ForkJoinPool.commonPool().getParallelism() + 1;

        CustomWorkerThreadFactory factory = new CustomWorkerThreadFactory(MY_NEWS_PROCESSOR);
        customThreadPool = new ForkJoinPool(parallelStreams, factory, null, false);
        usageMeter = new ThreadPoolUsageMeter(customThreadPool);

        IntStream.range(0, parallelStreams).forEach(s -> companiesHash.registerThread(MY_NEWS_PROCESSOR + s));
    }

    public int getNumberOfThreads() {
        return parallelStreams;
    }

    public Set<Integer> startNewsProcessing() {
        ForkJoinTask<Set<Integer>> foundCompanies = findCompaniesInNews();

        try {
            var result = foundCompanies.get();
            usageMeter.printUsage();
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return new HashSet<>();
    }

    private ForkJoinTask<Set<Integer>> findCompaniesInNews() {
        Stream<Optional<NewsDto>> newsStream = Stream.generate(this::generator);

        return customThreadPool.submit(
                () -> newsStream.unordered().parallel()
                        .takeWhile(n -> !n.isEmpty() && stopCondition.get() == false)
                        .map(news -> findCompaniesInNews(news.get()))
                        .reduce(new HashSet<>(), (a, b) -> {
                            HashSet<Integer> list = new HashSet<>(a);
                            list.addAll(b);
                            return list;
                        })
        );
    }

    private Optional<NewsDto> generator() {
        try {
            Optional<NewsDto> news = null;
            while (news == null) {
                news = newsQueue.poll(1, TimeUnit.SECONDS);
                if (stopCondition.get()) {
                    return Optional.empty();
                }
            }
            return news == null ? Optional.empty() : news;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }

    private Set<Integer> findCompaniesInNews(NewsDto news) {
        Set<Integer> result = new HashSet<>();

        usageMeter.monitorUsage();

        String[] newsSplits = news.getBody().split(" ");
        var threadId = Thread.currentThread().getName();

        int i = 0;
        while (i < newsSplits.length) {
            boolean found = false;
            while (i < newsSplits.length && companiesHash.isPartOfCompany(newsSplits[i].replaceAll("[^a-zA-Z0-9  &'/-]", "").trim(), threadId)) {
                i++;
                found = true;
            }
            if (found) {
                companiesHash.getCompanyId(threadId).ifPresent(id -> result.add(id));
                companiesHash.resetReading(threadId);
            } else {
                i++;
            }
        }

        return result;
    }
}

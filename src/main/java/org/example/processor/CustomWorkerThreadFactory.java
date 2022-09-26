package org.example.processor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;

public class CustomWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    private final AtomicLong id = new AtomicLong(0);

    private final String threadPrefix;

    CustomWorkerThreadFactory(String prefix) {
        threadPrefix = prefix;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName(threadPrefix + id.getAndIncrement());
        return worker;
    }

    String getPrefix() {
        return threadPrefix;
    }

}

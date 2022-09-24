package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolUsageMeter {
    private Map<String, AtomicLong> usedThreads = new HashMap<>();
    private final ForkJoinPool trackingPool;
    private final String threadPrefix;

    ThreadPoolUsageMeter(ForkJoinPool pool) {
        trackingPool = pool;
        threadPrefix = ((CustomWorkerThreadFactory)pool.getFactory()).getPrefix();

        for(int i =0; i < pool.getParallelism(); i++)
        {
            usedThreads.put(threadPrefix + i, new AtomicLong(0));
        }
    }

    void monitorUsage() {
        var key = Thread.currentThread().getName();
        if(usedThreads.containsKey(key)) {
            usedThreads.get(key).incrementAndGet();
        }
    }

    void printUsage() {
        var total = usedThreads.entrySet().stream().reduce(0L, (prev, e) -> {
            System.out.println("thread " + e.getKey() + " used " + e.getValue().get());
            return prev + e.getValue().get();
        }, (a,b) -> a+b);
        System.out.println("Total for " + threadPrefix + " is: " + total);
    }
}

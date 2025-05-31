package org.example.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomThreadFactory implements ThreadFactory {
    private static final String namePrefix = "CustomPool-worker-";
    private static final String runPrefix = "[ThreadFactory] Creating new thread:";

    private static final Logger logger = Logger.getLogger(CustomThreadFactory.class.getName());
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread instance = new Thread(r, namePrefix + threadNumber.getAndIncrement());

        logger.info(String.format("%s %s", runPrefix, instance.getName()));

        return instance;
    }
} 
package org.example.rxjava.scheduler;


public interface Scheduler {
    void execute(Runnable task);

    Worker createWorker();
} 
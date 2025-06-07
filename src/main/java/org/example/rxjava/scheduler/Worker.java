package org.example.rxjava.scheduler;

public interface Worker {
    void execute(Runnable task);
    void dispose();
}

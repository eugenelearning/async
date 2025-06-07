package org.example.rxjava.scheduler;

public class Schedulers {
    public static final Scheduler IO = new IoScheduler();
    public static final Scheduler COMPUTATION = new ComputationScheduler();
    public static final Scheduler SINGLE = new SingleScheduler();

    private Schedulers() {
        throw new IllegalStateException("Unknown scheduler type");
    }
} 
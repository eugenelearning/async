package com.example;


import org.example.rxjava.core.Observable;
import org.example.rxjava.core.Observer;
import org.example.rxjava.scheduler.Schedulers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RxJavaTest {

    @Test
    public void testBasicObservable() {
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
            observer.onNext(0);
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        }).subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Не должно быть ошибок");
            }

            @Override
            public void onComplete() {
                assertEquals(Arrays.asList(0, 1, 2, 3), results);
            }
        });
    }

    @Test
    public void testErrorHandling() {
        RuntimeException testException = new RuntimeException("Тестовая ошибка");
        AtomicInteger errorCount = new AtomicInteger(0);

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onError(testException);
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        assertEquals(testException, t);
                        errorCount.incrementAndGet();
                    }

                    @Override
                    public void onComplete() {
                        fail("Не должно быть завершения");
                    }
                });

        assertEquals(1, errorCount.get());
    }

    /**
     * Операторы
     */
    @Test
    public void testMapOperator() {
        List<String> results = new ArrayList<>();

        Observable.create((Observer<String> observer) -> {
                    observer.onNext("kreks");
                    observer.onNext("feks");
                    observer.onNext("peks");
                    observer.onComplete();
                })
                .map(i -> "Give me: " + i)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Не должно быть ошибок");
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(Arrays.asList("Give me: kreks", "Give me: feks", "Give me: peks"), results);
                    }
                });
    }

    @Test
    public void testFilterOperator() {
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onNext(3);
                    observer.onNext(4);
                    observer.onNext(5);
                    observer.onComplete();
                })
                .filter(i -> i % 2 == 0)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Не должно быть ошибок");
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(Arrays.asList(2, 4), results);
                    }
                });
    }

    @Test
    public void testFlatMapOperator() {
        List<String> results = new ArrayList<>();

        Observable.create((Observer<List<Integer>> observer) -> {
                    observer.onNext(List.of(1, 2, 3));
                    observer.onNext(List.of(4, 5));
                    observer.onNext(List.of(7));
                    observer.onComplete();
                })
                .flatMap(list -> Observable.create((Observer<String> observer) -> {
                    observer.onNext("Size " + list.size());
                    observer.onComplete();
                })).subscribe(new Observer<>() {
                    @Override
                    public void onComplete() {
                        assertEquals(Arrays.asList("Size 3", "Size 2", "Size 1"), results);
                    }

                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Не должно быть ошибок");
                    }
                });

    }


    @Test
    public void testAsyncExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> results = new ArrayList<>();

        Observable.create((Observer<Integer> observer) -> {
                    new Thread(() -> {
                        observer.onNext(1);
                        observer.onNext(2);
                        observer.onNext(3);
                        observer.onComplete();
                    }).start();
                })
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Не должно быть ошибок");
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(Arrays.asList(1, 2, 3), results);
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    /**
     * Планировщики
     */
    @Test
    public void testSchedulers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> results = new ArrayList<>();

        Observable.create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.COMPUTATION)
                .map(i -> "Processed: " + i)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Не должно быть ошибок");
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(Arrays.asList("Processed: 1", "Processed: 2", "Processed: 3"), results);
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }
} 
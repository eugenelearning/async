package org.example.rxjava.core;

import org.example.rxjava.scheduler.Scheduler;
import org.example.rxjava.scheduler.Worker;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final Subscriptable<T> source;

    private Observable(Subscriptable<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(Subscriptable<T> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<T> observer) {
        try {
            source.subscribe(observer);
            return new Disposable() {
                private boolean disposed = false;

                @Override
                public void dispose() {
                    disposed = true;
                }

                @Override
                public boolean isDisposed() {
                    return disposed;
                }
            };
        } catch (Exception e) {
            observer.onError(e);
            return Disposable.disposed();
        }
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer -> {
            Worker worker = scheduler.createWorker();
            worker.execute(() -> {
                try {
                    source.subscribe(observer);
                } catch (Exception e) {
                    observer.onError(e);
                }
            });
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer -> {
            Worker worker = scheduler.createWorker();
            subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    worker.execute(() -> observer.onNext(item));
                }

                @Override
                public void onError(Throwable t) {
                    worker.execute(() -> observer.onError(t));
                }

                @Override
                public void onComplete() {
                    worker.execute(observer::onComplete);
                }
            });
        });
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(observer ->
                subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            observer.onNext(mapper.apply(item));
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(observer ->
                subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            Observable<R> observable = mapper.apply(item);
                            observable.subscribe(new Observer<>() {
                                @Override
                                public void onNext(R value) {
                                    observer.onNext(value);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    observer.onError(t);
                                }

                                @Override
                                public void onComplete() {}
                            });
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }
}
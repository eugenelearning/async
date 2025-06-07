package org.example.rxjava.core;

@FunctionalInterface
public interface Subscriptable<T> {
    void subscribe(Observer<T> observer);
} 
package org.example.rxjava.core;

/**
 * Интерфейс для управления подписками на Observable
 */
public interface Disposable {
    /**
     * Создает новый отмененный Disposable
     *
     * @return новый отмененный Disposable
     */
    static Disposable disposed() {
        return new Disposable() {
            @Override
            public void dispose() {}

            @Override
            public boolean isDisposed() {
                return true;
            }
        };
    }

    /**
     * Отменяет подписку
     */
    void dispose();

    /**
     * Проверяет, отменена ли подписка
     *
     * @return true, если подписка отменена, false в противном случае
     */
    boolean isDisposed();
} 
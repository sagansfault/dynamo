package com.projecki.dynamo.game;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class GameSession {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private long start = 0L;
    private long elapsed = 0L;
    private boolean completed = false;

    public void start() {
        lock.writeLock().lock();
        try {
            if (!completed) {
                this.start = System.currentTimeMillis();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void stop() {
        lock.writeLock().lock();
        try {
            if (!completed) {
                this.elapsed += System.currentTimeMillis() - this.start;
                this.completed = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getElapsed() {
        lock.readLock().lock();
        try {
            return elapsed;
        } finally {
            lock.readLock().unlock();
        }
    }
}

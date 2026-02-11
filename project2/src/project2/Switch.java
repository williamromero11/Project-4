package project2;

import java.util.concurrent.locks.ReentrantLock;

public class Switch {
    private final int id;
    private final ReentrantLock lock;

    public Switch(int id) {
        this.id = id;
        this.lock = new ReentrantLock(true); // fair-ish locking
    }

    public int getId() {
        return id;
    }

    public boolean tryLock() {
        return lock.tryLock();
    }

    public void unlockIfHeldByCurrentThread() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}


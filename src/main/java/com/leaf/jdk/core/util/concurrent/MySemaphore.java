package com.leaf.jdk.core.util.concurrent;


import com.leaf.jdk.core.util.locks.MyAbstractQueuedSynchronizer;

import java.io.Serializable;

public class MySemaphore implements Serializable {

    private final MySync mySync;


    private static final long serialVersionUID = -3222578661600680210L;


    abstract static class MySync extends MyAbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1192457210091910933L;

        MySync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

    }


    static final class MyUnfairSync extends MySync {

        private static final long serialVersionUID = -2694183684443567898L;

        MyUnfairSync(int permits) {
            super(permits);
        }
    }


    static final class MyFairSync extends MySync {

        private static final long serialVersionUID = 2014338818796000944L;

        MyFairSync(int permits) {
            super(permits);
        }

    }

    public MySemaphore(int permits) {
        this.mySync = new MyUnfairSync(permits);
    }

    public MySemaphore(int permits, boolean fair) {
        mySync = fair ? new MyFairSync(permits) : new MyUnfairSync(permits);
    }


}

package com.leaf.jdk.core.util.concurrent;


import com.leaf.jdk.core.util.locks.MyAbstractQueuedSynchronizer;

import java.io.Serializable;

public class MySemaphore implements Serializable {

    private final MySync mySync;


    private static final long serialVersionUID = -3222578661600680210L;


    abstract static class MySync extends MyAbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1192457210091910933L;

        //permits 令牌数
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

        /**
         * @param permits 凭证（资源）
         */
        @Override
        protected void tryAcquireShared(int permits) {
            for (; ; ) {
                //创建Semaphore的时候 已经初始化了state的值
                int state = getState();
                //分配资源的检测  state 总资源 permits 当前需要分配的资源数 默认1
                int i = state - permits;
                //如果 i<0 说明资源不足 CAS修改当前所剩的资源数
                if (i < 0 || compareAndSetState(state, i)) {

                }

            }
        }
    }


    static final class MyFairSync extends MySync {

        private static final long serialVersionUID = 2014338818796000944L;

        MyFairSync(int permits) {
            super(permits);
        }

        @Override
        protected void tryAcquireShared(int permits) {

        }
    }

    public MySemaphore(int permits) {
        this.mySync = new MyUnfairSync(permits);
    }

    public MySemaphore(int permits, boolean fair) {
        mySync = fair ? new MyFairSync(permits) : new MyUnfairSync(permits);
    }


    public void acquire() {
        mySync.acquireSharedInterruptibly(1);
    }

}

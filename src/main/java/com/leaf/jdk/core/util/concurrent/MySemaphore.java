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

        /**
         * state+1
         *
         * @param arg 凭证
         * @return true CAS修改成功啦
         */
        protected boolean tryReleaseShared(int arg) {
            int current = getState();
            int next = current + arg;
            if (next < current) {
                throw new Error("参数异常！");
            }
            //多线程释放
            return compareAndSetState(current, next);
        }
    }


    static final class MyUnfairSync extends MySync {

        private static final long serialVersionUID = -2694183684443567898L;

        MyUnfairSync(int permits) {
            super(permits);
        }

        /**
         * @param permits 凭证（资源）
         * @return 返回获取成功标识 <0 没有资源分配；>0 有资源可以分配
         */
        @Override
        protected int tryAcquireShared(int permits) {
            for (; ; ) {
                //创建Semaphore的时候 已经初始化了state的值
                int state = getState();
                //分配资源的检测  state 总资源 permits 当前需要分配的资源数 默认1
                int i = state - permits;
                //如果 i<0 说明资源不足 CAS修改当前所剩的资源数
                if (i < 0 || compareAndSetState(state, i)) {
                    return i;
                }
            }
        }
    }


    static final class MyFairSync extends MySync {

        private static final long serialVersionUID = 2014338818796000944L;

        MyFairSync(int permits) {
            super(permits);
        }

        /**
         * 公平锁获取资源
         *
         * @param permits 资源 锁 凭证
         * @return 返回获取成功标识 -1 进入队列等待 ；<0 没有资源分配；>0 有资源可以分配
         */
        @Override
        protected int tryAcquireShared(int permits) {
            //只要还有凭证 i>0 就循环自旋获取锁
            for (; ; ) {
                if (hasQueuePred()) {
                    return -1;
                }
                //创建Semaphore的时候 已经初始化了state的值
                int state = getState();
                //分配资源的检测  state 总资源 permits 当前需要分配的资源数 默认1
                int i = state - permits;
                //如果 i<0 说明资源不足
                //那CAS呢 假设i=0 并且更新成功了 说明最后一个资源被拿到了 说明获取了锁；最后一次获取锁i=0
                //说明跳出循环只有两种情况 第一种i<0没有资源获取，或者CAS成功获取资源 ；
                if (i < 0 || compareAndSetState(state, i)) {
                    return i;
                }
            }
        }
    }

    public MySemaphore(int permits) {
        this.mySync = new MyUnfairSync(permits);
    }

    public MySemaphore(int permits, boolean fair) {
        mySync = fair ? new MyFairSync(permits) : new MyUnfairSync(permits);
    }


    /**
     * @throws InterruptedException 响应中断
     */
    public void acquire() throws InterruptedException {
        mySync.acquireSharedInterruptibly(1);
    }

    /**
     * InterruptedException 不响应中断
     */
    public void acquireUnInterruptibly() {
        mySync.acquireShared(1);
    }

    public boolean release() {
        return mySync.releaseShared(1);
    }


}

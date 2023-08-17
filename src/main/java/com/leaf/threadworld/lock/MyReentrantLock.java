package com.leaf.threadworld.lock;

import com.leaf.threadworld.aqs.MyAbstractQueuedSynchronizer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyReentrantLock implements Lock {


    /**
     * (MyFairSync | MyUnFairSync) extends MySync
     */
    abstract static class MySync extends MyAbstractQueuedSynchronizer {
        abstract void lock();
    }

    /**
     * 公平锁(可重入)
     */
    static class MyFairSync extends MySync {

        //公平锁加锁
        @Override
        void lock() {

            //参数1有两个功能
            //1.首次加锁
            //2.重入
            acquire(1);

        }

        /**
         * 尝试快速获取锁
         */
        @Override
        protected boolean tryAcquire(int args) {

            Thread currentThread = Thread.currentThread();
            int state = getState();
            //state==0 表示没有线程获取锁
            if (state == 0) {
                //先检查队列 如果没有线程排队则CAS抢锁 否则入队等待
                if (!hasQueuePred() && compareAndSetState(0, args)) {
                    setOwnerThread(currentThread);
                    return true;
                }
            } else if (currentThread == getOwnerThread()) {
                //判断当前线程是否已经拥有锁 可重入
                int newState = state + args;
                if (newState < 0) {
                    throw new Error("数据异常！");
                }
                setState(newState);
                return true;
            }
            return false;
        }
    }

    /**
     * 非公平锁
     */
    static class MyUnFairSync extends MySync {
        @Override
        void lock() {
            //上来就CAS操作，成功直接设置状态为1，表示获取到锁（非公平性 插队抢锁）
            if (compareAndSetState(0, 1)) {
                setOwnerThread(Thread.currentThread());
            } else {
                acquire(1);
            }

        }

        @Override
        protected boolean tryAcquire(int args) {

            Thread currentThread = Thread.currentThread();
            int state = getState();
            //state==0 表示没有线程获取锁
            if (state == 0) {
                //非公平锁 不需要检查队列 直接CAS抢锁
                if (compareAndSetState(0, args)) {
                    setOwnerThread(currentThread);
                    return true;
                }
            } else if (currentThread == getOwnerThread()) {
                //判断当前线程是否已经拥有锁 可重入
                int newState = state + args;
                if (newState < 0) {
                    throw new Error("数据异常！");
                }
                setState(newState);
                return true;
            }
            return false;
        }
    }


    /**
     * 加锁
     */
    @Override
    public void lock() {
        //公平锁 非公平锁


    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}

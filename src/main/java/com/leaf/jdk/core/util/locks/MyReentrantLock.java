package com.leaf.jdk.core.util.locks;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyReentrantLock implements Lock {


    private final MySync sync;

    public MyReentrantLock() {
        sync = new MyUnFairSync();
    }

    public MyReentrantLock(boolean fair) {
        sync = fair ? new MyFairSync() : new MyUnFairSync();
    }

    /**
     * (MyFairSync | MyUnFairSync) extends MySync
     */
    abstract static class MySync extends MyAbstractQueuedSynchronizer {
        abstract void lock();

        protected boolean tryRelease(int arg) {
            int i = getState() - arg;
            if (Thread.currentThread() != getOwnerThread()) {
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (i == 0) {
                free = true;
                setOwnerThread(null);
            }
            setState(i);//走到这里表示重入
            return free;
        }
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
                //这里有个大前提，就是释放了锁，也就是state=0
                //并不是网上所说到队列里面有元素就不会走 而是当前锁释放 就会走了
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

        /**
         * 抢锁
         *
         * @param args 1 状态值 默认state 0 无锁状态 ；1 有锁状态
         * @return
         */
        @Override
        protected boolean tryAcquire(int args) {

            Thread currentThread = Thread.currentThread();
            int state = getState();
            //state==0 表示没有线程获取锁
            if (state == 0) {
                //非公平锁 不需要检查队列 直接CAS抢锁
                if (compareAndSetState(0, args)) {
                    //设置当前线程到ASQ属性中
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
        sync.lock();
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

    /**
     * 释放锁 逻辑都是一样的
     * 1.state 减1 如果不是0 说明是重入；state=0后，owner = null
     * 2.唤醒head节点的后继节点
     */
    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    /**
     * 获取队列中的线程
     *
     * @return
     */
    public List<Thread> getQueueThreads() {
        return sync.getQueueThreads();
    }

}

package com.leaf.threadworld.test;

import com.leaf.threadworld.lock.MyReentrantLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class MyReentrantLockTest {
    static int num = 0;

    public static void main(String[] args) throws InterruptedException {
        Lock lock = new MyReentrantLock(true);

        test01(lock);
        test02(lock);
    }

    //验证原子性1
    public static void test01(Lock lock) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        System.out.println("========测试开始=======！");
        Thread t1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("t1 start");
                Thread.sleep(3000);
                System.out.println("t1 end");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
                lock.unlock();
            }

        }, "t1");

        Thread.sleep(100);

        Thread t2 = new Thread(() -> {
            //lock要写在try外部 如果写在内部 lock内的异常肯能会被try捕获
            lock.lock();
            try {
                System.out.println("t2 start");
                Thread.sleep(3000);
                System.out.println("t2 end");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
                lock.unlock();
            }

        }, "t2");
        t1.start();
        t2.start();
        countDownLatch.await();
        System.out.println("===============测试完成！");
    }

    //验证原子性2
    public static void test02(Lock lock) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                try {
                    lock.lock();
                    for (int j = 0; j < 889623; j++) {
                        num++;
                    }
                } finally {
                    countDownLatch.countDown();
                    lock.unlock();
                }
            }, "threadName" + i).start();
        }
        countDownLatch.await();
        System.out.println("expect num = 17792460 update num = " + num);
    }





}

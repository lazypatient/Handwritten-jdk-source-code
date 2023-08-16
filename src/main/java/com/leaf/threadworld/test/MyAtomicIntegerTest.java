package com.leaf.threadworld.test;

import com.leaf.threadworld.atom.MyAtomicInteger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class MyAtomicIntegerTest {

    //默认初始化了ValueAndVersion 0 0
    private static final MyAtomicInteger myAtomicInteger = new MyAtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        /*
          测试原子性
         */
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    myAtomicInteger.incrementAndGet();
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        System.out.println("期望结果是1000000========实际结果是========" + myAtomicInteger.getValue());
        if (myAtomicInteger.getValue() == 1000000) {
            System.out.println("==========================================原子性测试成功！");
        }


        /*
          测试ABA
         */
        System.out.println("======================验证ABA问题==========================");


        Thread t4 = new Thread(() -> {
            LockSupport.park();

            boolean flag = myAtomicInteger.compareAndSet(1000000, 200, myAtomicInteger.getVersion(), myAtomicInteger.getVersion() + 1);
            System.out.println(Thread.currentThread().getName()
                    + "======end=====获取的结果是" + myAtomicInteger.getValue() + "版本号是====="
                    + myAtomicInteger.getVersion() + "更新的结果是========" + flag);
        }, "t4");


        Thread t3 = new Thread(() -> {
            LockSupport.park();
            // myAtomicInteger.compareAndSet(1000000, 99);
            System.out.println(Thread.currentThread().getName() + "======end=====获取的结果是" + myAtomicInteger.getValue());

            int version = myAtomicInteger.getVersion();
            System.out.println("第一次版本号=====>" + version + "结果是=====>" + myAtomicInteger.getValue());

            myAtomicInteger.compareAndSet(1000000, 108, version, version + 1);
            int newVersion = myAtomicInteger.getVersion();
            System.out.println("第二次版本号=====>" + newVersion + "结果是=====>" + myAtomicInteger.getValue());

            myAtomicInteger.compareAndSet(108, 1000000, newVersion, newVersion + 1);
            int new2Version = myAtomicInteger.getVersion();
            System.out.println("第三次版本号=====>" + new2Version + "结果是=====>" + myAtomicInteger.getValue());
            LockSupport.unpark(t4);

        }, "t3");


        Thread t2 = new Thread(() -> {
            LockSupport.park();
            myAtomicInteger.compareAndSet(66, 1000000);
            System.out.println(Thread.currentThread().getName() + "======end=====获取的结果是" + myAtomicInteger.getValue());
            LockSupport.unpark(t3);
        }, "t2");


        Thread t1 = new Thread(() -> {
            myAtomicInteger.compareAndSet(1000000, 66);
            System.out.println(Thread.currentThread().getName() + "======end=====获取的结果是" + myAtomicInteger.getValue());
            LockSupport.unpark(t2);
        }, "t1");


        t1.start();
        t2.start();
        t3.start();
        t4.start();


    }
}

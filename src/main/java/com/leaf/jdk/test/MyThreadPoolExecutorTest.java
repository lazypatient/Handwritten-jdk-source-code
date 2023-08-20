package com.leaf.jdk.test;

import java.util.concurrent.locks.LockSupport;

public class MyThreadPoolExecutorTest {
    public static void main(String[] args) throws InterruptedException {

        Thread mainThread = Thread.currentThread();
        Thread t2 = new Thread(() -> {
            System.out.println("2222222");
            //count--->1
            LockSupport.unpark(mainThread);
            System.out.println("3333333");
//            System.out.println("1");
//            LockSupport.park();
//            System.out.println("2");
            mainThread.interrupt();

            System.out.println(mainThread.isInterrupted());
            System.out.println(mainThread.isInterrupted());


        });
        t2.start();
//        new Thread(() -> {
//
//            mainThread.interrupt();
//            System.out.println("flag interrupt===>" + mainThread.isInterrupted());
//            System.out.println("flag interrupt===>" + mainThread.isInterrupted());
//
//        }, "t3").start();

//        Thread.sleep(2000);
        //先判断count 是 满足唤醒条件 在 count-1 = 0  count=0
        LockSupport.park();
        System.out.println("444444444");

        LockSupport.park();


        System.out.println("11111111");
        LockSupport.park();
        System.out.println("88888888");
        LockSupport.park();
        System.out.println("99999999");
        int temp = 0;

        for (int i = 0; i < 1000000000; i++) {

            temp++;
        }

        System.out.println("main " + Thread.interrupted());
        System.out.println("main " + Thread.interrupted());
        System.out.println("101010100101");
        LockSupport.park();
        System.out.println("90909090");

        LockSupport.park();
        System.out.println("2020200202020");

        LockSupport.park();
        System.out.println("78787878787878");

//牛逼
//        System.out.println(888);
//
//        Thread.sleep(200);
//
//        LockSupport.unpark(t2);
//        Thread.sleep(5000);
//        LockSupport.unpark(t2);


    }
}

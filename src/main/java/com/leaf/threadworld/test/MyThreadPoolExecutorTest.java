package com.leaf.threadworld.test;

import java.util.concurrent.locks.LockSupport;

public class MyThreadPoolExecutorTest {
    public static void main(String[] args) throws InterruptedException {


        Thread t2 = new Thread(() -> {
            LockSupport.park();
            System.out.println("1");
            LockSupport.park();
            System.out.println("2");


        });
        t2.start();
        System.out.println(888);

        Thread.sleep(200);

        LockSupport.unpark(t2);
        Thread.sleep(5000);
        LockSupport.unpark(t2);


    }
}

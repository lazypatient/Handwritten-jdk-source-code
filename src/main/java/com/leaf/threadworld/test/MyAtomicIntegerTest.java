package com.leaf.threadworld.test;

import com.leaf.threadworld.atom.MyAtomicInteger;

import java.util.concurrent.CountDownLatch;

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




    }
}

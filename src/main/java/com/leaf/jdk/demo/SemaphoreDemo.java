package com.leaf.jdk.demo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 信号量
 * 1.设置资源数
 * 2.指定 资源 和 共享线程数
 * 3.申请资源 释放资源
 */
public class SemaphoreDemo {
    private static boolean isAcquired = false;


    public static void main(String[] args) {

        //分配2个资源
        Semaphore semaphore = new Semaphore(1);

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    //消耗一个资源 state-1
                    semaphore.acquire();
                    isAcquired = true;
                    //业务
                    System.out.println(Thread.currentThread().getName() + "is running!");
                    TimeUnit.SECONDS.sleep(5);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放资源
                    //state+1
                    if (!isAcquired) {
                        System.out.println("当前线程" + Thread.currentThread().getName() + "发生了中断！");
                    }
                    //正常释放资源
                    semaphore.release();
                }

            }, "tName===" + i).start();
        }


    }
}

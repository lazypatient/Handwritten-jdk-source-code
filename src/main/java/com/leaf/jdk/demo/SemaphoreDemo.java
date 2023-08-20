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
    private static final Semaphore semaphore = new Semaphore(1);


    public static void main(String[] args) throws InterruptedException {

        //分配2个资源
        Thread t1 = new Thread(SemaphoreDemo::task, "001");
        Thread t2 = new Thread(SemaphoreDemo::task, "002");

        t1.start();
        TimeUnit.MILLISECONDS.sleep(100);
        t2.start();
        t2.interrupt();


    }

    public static void task() {
        try {
            //消耗一个资源 state-1
            semaphore.acquire();
            //业务
            System.out.println(Thread.currentThread().getName() + "  is running!");
            TimeUnit.SECONDS.sleep(5);
            isAcquired = true;


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //释放资源
            //state+1
            if (!isAcquired) {
                System.out.println("当前线程" + Thread.currentThread().getName() + "发生了中断！资源还剩余" + semaphore.availablePermits());

            } else {
                //正常释放资源
                System.out.println(Thread.currentThread().getName() + "正常释放了锁！");
                semaphore.release();
            }

        }
    }
}

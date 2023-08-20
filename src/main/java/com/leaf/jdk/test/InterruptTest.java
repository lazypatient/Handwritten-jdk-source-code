package com.leaf.jdk.test;

public class InterruptTest {
    public static void main(String[] args) {

        Thread mainThread = Thread.currentThread();
        System.out.println(mainThread.isInterrupted());

        mainThread.interrupt();

        System.out.println(mainThread.isInterrupted());
        System.out.println(mainThread.isInterrupted());
        System.out.println(mainThread.isInterrupted());
        //获取当前线程的中断结果 重置中断标记
        System.out.println(Thread.interrupted());
        System.out.println(Thread.interrupted());
        System.out.println(mainThread.isInterrupted());









    }
}

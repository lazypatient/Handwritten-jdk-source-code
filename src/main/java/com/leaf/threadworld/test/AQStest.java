package com.leaf.threadworld.test;

public class AQStest {
    public static void main(String[] args) {
        int a = 1;
        int b = 12;
        int c = 20;
        a = b = c = 99;
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

    }
}

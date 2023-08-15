package com.leaf.threadworld.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class MyUnsafe {

    private static final Unsafe theUnsafe = getUnsafeByReflectConstruct();

    private static final AtomicInteger num = new AtomicInteger(0);


    /**
     * 获取unsafe的三种方式
     */

    public static Unsafe getUnsafeByReflectProperty() {

        Unsafe unsafe;
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return unsafe;
    }

    public static Unsafe getUnsafeByReflectConstruct() {
        Unsafe unsafe;
        try {
            Constructor<?> constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

            unsafe = (Unsafe) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return unsafe;
    }

//    public void allocateMemory(long bytes) {
//        theUnsafe.allocateMemory(bytes);
//    }


    public static void main(String[] args) throws NoSuchFieldException {
        //验证成功
        //sun.misc.Unsafe@75b84c92
        //sun.misc.Unsafe@232204a1
//        System.out.println(getUnsafeByReflectConstruct());
//        System.out.println(getUnsafeByReflectProperty());
//        Unsafe unsafe = MyUnsafe.getUnsafeByReflectProperty();
//
//        long address = memorySth();
//        unsafe.putLong(address, 1223);
//        long aLong = unsafe.getLong(address);
//        System.out.println(aLong);


        Unsafe unsafe = getUnsafeByReflectProperty();
        class Person {
            int age = 12;
        }

        Person person = new Person();
        System.out.println(person.age);

        long ageAddress = unsafe.objectFieldOffset(person.getClass().getDeclaredField("age"));

        unsafe.putInt(person, ageAddress, 80);
        System.out.println(person.age);


//        System.out.println(unsafe.getAddress(a));
//        long address = unsafe.allocateMemory(1);
//        unsafe.putByte(address, (byte) 1);
//        byte aByte = unsafe.getByte(address);
//        System.out.println(aByte);

//        long l = unsafe.objectFieldOffset(byte.class.getDeclaredField("aByte"));
//        System.out.println(l);


//        new Thread(() -> {
//            for (; ; ) {
//                if (aByte == 5) {
//                    System.out.println("start to break;");
//                    break;
//                }
//            }
//        }).start();

//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        int i = unsafe.arrayBaseOffset(int[].class);
//        unsafe.putOrderedInt(aByte, i, 20);
////        unsafe.putByte(address, (byte) 10);
//        byte newVal = unsafe.getByte(address);
//
//        System.out.println(newVal);

        //三个参数 第一个是操作的obj 第二个是offset偏移量 通过offset可以找到obj内的属性 第三个是要对属性修改的值
        //unsafe.putIntVolatile();
//        String[] msg = new String[]{"aa", "bb", "cc", "dd"};
//        int arrayBaseOffset = unsafe.arrayBaseOffset(String[].class);
////        System.out.println(arrayBaseOffset);
//        int arrayIndexScale = unsafe.arrayIndexScale(String[].class);
////        System.out.println(arrayIndexScale);
//
//        long offset = 0;
//        for (int i = 0; i < msg.length; i++) {
//            //计算偏移
//
//            offset = arrayBaseOffset + (long) i * arrayIndexScale;
//            String object = (String) unsafe.getObject(msg, offset);
//            System.out.println(object);
//
//        }
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        new Thread(() -> {
//            for (; ; ) {
//                if (num.get() == 20) {
//                    System.out.println("break t2 thread loop.");
//                    break;
//                }
//            }
//        }, "t3").start();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
////        num.set(12);
//        System.out.println("main thread start to change num = 20!");
//        num.lazySet(20);
//

//        int baseOffset = unsafe.arrayBaseOffset(int[].class);
//        int[] a = {1, 2, 3, 4};
////        int[] b = new int[4];
//        int[] b = {0,0,0,0};
//
//        unsafe.copyMemory(a, baseOffset, b, baseOffset, 16);
//        System.out.println(Arrays.toString(b));
////        unsafe.setMemory(a, baseOffset, 8, (byte) 1);
//        String str = "Hello, World!";
//        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
//        int sizeInBytes = bytes.length;
//        System.out.println("Size in bytes: " + sizeInBytes);


        //如果用0填充 其实就是初始化内存
//        long address = unsafe.allocateMemory(16);
//        //每个字节存1
//        unsafe.setMemory(address, 16, (byte) 1);
////        unsafe.freeMemory(address);
//        //0000000100000001000000010000000100000001000000010000000100000001
//        System.out.println(unsafe.getLong(address));
        //char 两个字节       0000000100000001
//        System.out.println((int)unsafe.getChar(address));

        /*
        三者存在区别
        1.普通赋值
        unsafe.putObject();
        2.volatile 赋值 保证可见性 有序性
        unsafe.putObjectVolatile();
        3.1和2对情况交替出现 有jvm本身优化确定
        unsafe.putOrderedObject();
         */
    }

//    unsafe 操作内存

//    public static long memorySth() {
//
//        Unsafe unsafe = MyUnsafe.getUnsafeByReflectProperty();
//
//        //分配内存 内存对齐 堆外内存
//        long address = unsafe.allocateMemory(16);
//        //在某个地址存值
//        unsafe.putLong(address, 12);
//
////        unsafe.copyMemory();
//        long newAddress = unsafe.allocateMemory(16);
//        unsafe.copyMemory(address, newAddress, 16);
//        long aLong = unsafe.getLong(newAddress);
//        System.out.println("copy res====>" + aLong);
//
//        System.out.println("unsafe=====>" + address);
//        //释放内存
//        unsafe.freeMemory(address);
//
//        return address;
//
    //unsafe.putObject() 对象属性赋值


    //cas核心
    //Atomic::cmpxchg(x,addr,e)
    //x update   addr address  e expect

//    }


}

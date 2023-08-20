package com.leaf.jdk.core.sun.misc;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

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
}

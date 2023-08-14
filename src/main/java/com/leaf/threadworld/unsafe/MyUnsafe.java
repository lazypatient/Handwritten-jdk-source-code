package com.leaf.threadworld.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class MyUnsafe {


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

    //验证成功
    //sun.misc.Unsafe@75b84c92
    //sun.misc.Unsafe@232204a1
    public static void main(String[] args) {
        System.out.println(getUnsafeByReflectConstruct());
        System.out.println(getUnsafeByReflectProperty());
    }
}

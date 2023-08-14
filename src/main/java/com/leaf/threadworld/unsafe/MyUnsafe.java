package com.leaf.threadworld.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class MyUnsafe {

    public static Unsafe getUnsafe() {

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
}

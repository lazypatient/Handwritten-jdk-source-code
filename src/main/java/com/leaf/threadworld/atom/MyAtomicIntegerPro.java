package com.leaf.threadworld.atom;


import com.leaf.threadworld.unsafe.MyUnsafe;
import sun.misc.Unsafe;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * MyAtomicInteger -----> MyAtomicIntegerPro
 * 对ABA功能进行了通用抽象封装 保留了部分int方法 如果确实是int 类型可以使用
 */
public class MyAtomicIntegerPro<V> implements java.io.Serializable {

    private static final long serialVersionUID = 6214790243416807050L;

    private static final Unsafe unsafe = MyUnsafe.getUnsafeByReflectProperty();

    static class ValueAndVersion<T> {

        private volatile T value;
        private volatile int version;

        private ValueAndVersion(T value, int version) {
            this.value = value;
            this.version = version;
        }

    }

    private volatile ValueAndVersion<V> vav;

    private static final long valueOffset;
    private static final long vavOffset;


    static {
        try {

            valueOffset = unsafe.objectFieldOffset
                    (ValueAndVersion.class.getDeclaredField("value"));


            vavOffset = unsafe.objectFieldOffset
                    (MyAtomicInteger.class.getDeclaredField("vav"));

        } catch (Exception ex) {
            throw new Error(ex);
        }
    }


    public MyAtomicIntegerPro(V initialValue, int initialVersion) {
        this.vav = new ValueAndVersion(initialValue, initialVersion);
    }

    public MyAtomicIntegerPro(V initialValue) {
        this.vav = new ValueAndVersion(initialValue, -1);
    }

    public MyAtomicIntegerPro() {
        this.vav = new ValueAndVersion(0, 0);
    }


    public final ValueAndVersion<V> get() {
        return this.vav;
    }

    public final V getValue() {
        return this.vav.value;
    }

    public final int getVersion() {
        return this.vav.version;
    }


    public final void set(V newValue) {
        this.vav.value = newValue;
    }

    public final void set(V newValue, int newVersion) {
//        value = newValue;
        if (newValue != this.vav.value || newVersion != this.vav.version) {
            this.vav = new ValueAndVersion(newValue, newVersion);
        }
    }


    public final void lazySet(V newValue) {
        unsafe.putOrderedObject(this.vav, valueOffset, newValue);
    }

    public final void lazySet(V newValue, int newVersion) {
        unsafe.putOrderedObject(this, vavOffset, new ValueAndVersion(newValue, newVersion));
    }


    public final V getAndSet(V newValue) {
        return (V) unsafe.getAndSetObject(this.vav, valueOffset, newValue);
    }


    public final boolean compareAndSet(V expectValue, V updateValue) {
        ValueAndVersion currVav = this.vav;

        return expectValue == currVav.value &&
                ((updateValue == currVav.value) ||
                        (unsafe.compareAndSwapObject(this, vavOffset, currVav, new ValueAndVersion(updateValue, -1))));
    }

    public final boolean compareAndSet(V expectValue, V updateValue, int expectVersion, int updateVersion) {

        ValueAndVersion currVav = this.vav;

        return expectValue == currVav.value && expectVersion == currVav.version &&
                ((updateValue == currVav.value && updateVersion == currVav.version) ||
                        (unsafe.compareAndSwapObject(this, vavOffset, currVav, new ValueAndVersion(updateValue, updateVersion))));
    }


    public final boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this.vav, valueOffset, expect, update);
    }


    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this.vav, valueOffset, 1);
    }


    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this.vav, valueOffset, -1);
    }


    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this.vav, valueOffset, delta);
    }


    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this.vav, valueOffset, 1) + 1;
    }


    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this.vav, valueOffset, -1) - 1;
    }


    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this.vav, valueOffset, delta) + delta;
    }


//    public final int getAndUpdate(IntUnaryOperator updateFunction) {
//        int prev, next;
//        do {
//            prev = getValue();
//            next = updateFunction.applyAsInt(prev);
//        } while (!compareAndSet(prev, next));
//        return prev;
//    }


//    public final int updateAndGet(IntUnaryOperator updateFunction) {
//        int prev, next;
//        do {
//            prev = getValue();
//            next = updateFunction.applyAsInt(prev);
//        } while (!compareAndSet(prev, next));
//        return next;
//    }


//    public final int getAndAccumulate(int x,
//                                      IntBinaryOperator accumulatorFunction) {
//        int prev, next;
//        do {
//            prev = getValue();
//            next = accumulatorFunction.applyAsInt(prev, x);
//        } while (!compareAndSet(prev, next));
//        return prev;
//    }


//    public final int accumulateAndGet(int x,
//                                      IntBinaryOperator accumulatorFunction) {
//        int prev, next;
//        do {
//            prev = getValue();
//            next = accumulatorFunction.applyAsInt(prev, x);
//        } while (!compareAndSet(prev, next));
//        return next;
//    }


    public String toString() {
        return Integer.toString((int) getValue());
    }

    public int intValue() {
        return (int) getValue();
    }


    public long longValue() {
        return (long) getValue();
    }


    public float floatValue() {
        return (float) getValue();
    }

    public double doubleValue() {
        return (double) getValue();
    }

}

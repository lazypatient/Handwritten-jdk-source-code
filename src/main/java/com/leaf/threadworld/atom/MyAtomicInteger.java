package com.leaf.threadworld.atom;


import com.leaf.threadworld.unsafe.MyUnsafe;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * JDK提供的AtomicInteger无法解决ABA问题
 * MyAtomicInteger代码进行了优化，完美解决ABA问题
 */
public class MyAtomicInteger extends Number implements java.io.Serializable {

    private static final long serialVersionUID = 6214790243416807050L;

    private static final Unsafe unsafe = MyUnsafe.getUnsafeByReflectProperty();

    static class ValueAndVersion {

        private volatile int value;
        private volatile int version;

        private ValueAndVersion(int value, int version) {
            this.value = value;
            this.version = version;
        }

    }

    private volatile ValueAndVersion vav;

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


    public MyAtomicInteger(int initialValue, int initialVersion) {
        this.vav = new ValueAndVersion(initialValue, initialVersion);
    }


    public MyAtomicInteger() {
        this.vav = new ValueAndVersion(0, 0);
    }


    public final ValueAndVersion get() {
        return this.vav;
    }

    public final int getValue() {
        return this.vav.value;
    }

    public final int getVersion() {
        return this.vav.version;
    }


    public final void set(int newValue) {
        this.vav.value = newValue;
    }

    public final void set(int newValue, int newVersion) {
//        value = newValue;
        if (newValue != this.vav.value || newVersion != this.vav.version) {
            this.vav = new ValueAndVersion(newValue, newVersion);
        }
    }


    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this.vav, valueOffset, newValue);
    }

    public final void lazySet(int newValue, int newVersion) {
        unsafe.putOrderedObject(this, vavOffset, new ValueAndVersion(newValue, newVersion));
    }


    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }


    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this.vav, valueOffset, expect, update);
    }

    public final boolean compareAndSet(int expectValue, int updateValue, int expectVersion, int updateVersion) {

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


    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = getValue();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }


    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = getValue();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }


    public final int getAndAccumulate(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = getValue();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }


    public final int accumulateAndGet(int x,
                                      IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = getValue();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }


    public String toString() {
        return Integer.toString(getValue());
    }

    public int intValue() {
        return getValue();
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

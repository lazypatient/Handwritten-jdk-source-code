package com.leaf.threadworld.aqs;


import com.leaf.threadworld.unsafe.MyUnsafe;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/*
    根据可重入锁绘制AQS
    AQS = CAS+CLH双向队列+fifo+LockSupport
       +------+  prev +------+       +------+
       |      | <---- |      | <---- |      |
 head  | node |  next | node |       | node |  tail
       |      | ----> |      | ----> |      |
       +------+       +------+       +------+
 */
public abstract class MyAbstractQueuedSynchronizer {

    private static final Unsafe UNSAFE = MyUnsafe.getUnsafeByReflectProperty();

    //state 偏移量
    private static long stateOffset;

    private static long tailOffset;

    private static long headOffset;


    static {
        try {
            stateOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("state"));
            tailOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            headOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("head"));

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    private volatile Node head;

    private volatile Node tail;

    private Thread ownerThread;

    private volatile int state;

    static class Node {

        //共享 ｜ 独占
        Node nextWaiter;
        //独占
        static Node EXCLUSIVE = null;


        private volatile Node prev;
        private volatile Node next;
        private volatile Thread thread;

        public Node() {

        }

        public Node(Thread thread, Node mode) {
            this.thread = thread;
            this.nextWaiter = mode;
        }

    }


    //CAS state
    public boolean compareAndSetState(int expect, int update) {
        return UNSAFE.compareAndSwapInt(this, stateOffset, expect, update);
    }

    //抢锁
    public void acquire(int args) {
        //尝试快速获取锁 如果失败需要  1.入队   2.自旋
        if (!tryAcquire(args) && addNode(Node.EXCLUSIVE) && acquireQuene()) {


        }

    }

    /**
     * 入队
     */
    public Node addNode(Node mode) {

        //分两种情况 但都是尾部入队
        //1.第一次入队 需要初始化head tail mode节点
        //2.非第一次入队 调整head tail pred next cas 操作
        Node node = new Node(Thread.currentThread(), mode);
        //获取当前队列最后一个节点
        Node pred = tail;
        if (pred != null) { //这里其实可以优化 jdk9已经进行了优化
            //新增节点的前节点就是当前队列的最后节点
            //oldNode<-----newNode
            node.prev = pred;
            //CAS后 tail就指向了新增的node节点 tail---->newNode
            if (compareAndSetTail(pred, node)) {
                //oldNode---->newNode
                pred.next = node;
                return node;
            }
        }
        //走到这里说明  1.队列没有初始化 第一次入队 2.node CAS失败 需要自旋入队
        casEnqueue(node);

        return null;
    }

    private void casEnqueue(Node node) {
        for (; ; ) {
            Node tempTail = tail;
            if (tempTail == null) {
                //此时需要初始化
                if (compareAndSetHead(new Node())) {
                    tail = head;
                }
            } else {
                //重新入队 代码一样的
                node.prev = tempTail;
                if (compareAndSetTail(tempTail, node)) {
                    tempTail.next = node;
                    //循环入队结束 跳出循环
                    return;
                }
            }
        }
    }

    private boolean compareAndSetHead(Node update) {
        return UNSAFE.compareAndSwapObject(this, headOffset, null, update);
    }

    private boolean compareAndSetTail(Node expect, Node update) {

        return UNSAFE.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * 自旋
     */
    public boolean acquireQuene() {

        return false;
    }


    //公平锁和非公平锁需要实现各种逻辑 模版设计模式
    protected boolean tryAcquire(int args) {
        throw new UnsupportedOperationException();
    }


    /**
     * 判断CLH对列有没有节点在排队
     */
    public boolean hasQueuePred() {


        return false;
    }


    public Thread getOwnerThread() {
        return ownerThread;
    }

    public void setOwnerThread(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Node getHead() {
        return head;
    }

    public void setHead(Node head) {
        this.head = head;
    }

    public Node getTail() {
        return tail;
    }

    public void setTail(Node tail) {
        this.tail = tail;
    }
}

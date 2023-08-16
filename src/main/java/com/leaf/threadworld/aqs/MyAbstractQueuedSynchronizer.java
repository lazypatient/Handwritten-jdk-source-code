package com.leaf.threadworld.aqs;


import com.leaf.threadworld.unsafe.MyUnsafe;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

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

    private static long waitStateOffset;

    private static long nextStateOffset;


    static {
        try {
            stateOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("state"));
            tailOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            headOffset = UNSAFE.objectFieldOffset(MyAbstractQueuedSynchronizer.class.getDeclaredField("head"));
            waitStateOffset = UNSAFE.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
            nextStateOffset = UNSAFE.objectFieldOffset(Node.class.getDeclaredField("next"));
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

        //节点等待状态
        private volatile int waitStatus;
        private static int CANCELED = 1;
        private static int SIGNAL = -1;//前区节点是SIGNAL 后继节点才可以阻塞


        public Node() {

        }

        public Node(Thread thread, Node mode) {
            this.thread = thread;
            this.nextWaiter = mode;
        }

        public Node preNode() {
            Node p = prev;
            if (p == null) {
                throw new NullPointerException();
            } else {
                return p;
            }
        }
    }

    //抢锁
    public void acquire(int args) {
        //尝试快速获取锁 如果失败需要  1.入队   2.自旋
        if (!tryAcquire(args) && acquireQuene(addNode(Node.EXCLUSIVE), args)) {


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

        return node;
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


    /**
     * 自旋 抢锁 阻塞
     */
    public boolean acquireQuene(Node node, int args) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                //1.尝试快速获取锁
                //如果前区节点是头节点 则快速尝试获取锁CAS
                Node preNode = node.preNode();
                if (preNode == head && tryAcquire(args)) {
                    //获取锁成功后
                    setHead(node);
                    preNode.next = null;
                    failed = false;
                    return interrupted;
                }
                //2.阻塞 LockSupport.park
                if (shouldParkAfterFailedAcquire(preNode, node) && parkAndCheckInterupt()) {
                    interrupted = true;
                }
            }
        } finally {
            //取消 就是把自己从对列中摘除
            if (failed) {
                cancelAcquire(node);
            }
        }


    }

    private void cancelAcquire(Node node) {
        if (node == null) {
            return;
        }
        node.thread = null;
        Node pred = node.prev;
        for (; ; ) {
            if (pred.waitStatus > 0) {
                //跳过CANCELLED状态的节点
                node.prev = pred = pred.prev;
            }
            Node predNext = pred.next;
            //设置当前节点状态是CANCELLED
            node.waitStatus = Node.CANCELED;
            //分2种情况
            //1.如果是尾节点 CAS修改
            if (node == tail && compareAndSetTail(node, pred)) {
                //把尾节点设置为null
                compareAndSetNext(pred, predNext, null);
            }
            //2.假如改节点是中间节点
            else {
                int ws = 0;
                if (pred != head
                        && (
                        (ws = pred.waitStatus) == Node.SIGNAL)
                        ||
                        (ws <= 0 && compareAndSetWaitState(pred, ws, Node.SIGNAL))
                ) {

                }
            }


        }

    }


    private boolean parkAndCheckInterupt() {
        //阻塞的线程会卡在这里等待唤醒
        //unpark ，interrupt 唤醒方式
        LockSupport.park(this);
        return Thread.interrupted();//清除标记位 在某处还原 业务流出不能破坏
        //这里为什么要清除标记位置呢？
        //LockSupport 一旦被中断 不清除中断标记 那么将无法在park
        //C++ openjdk源码 一旦检测 中断标记为 直接结束 不会执行阻塞

    }

    private boolean shouldParkAfterFailedAcquire(Node pred, Node node) {

        int waitStatus = pred.waitStatus;
        //前驱节点的状态是SIGNAL就阻塞
        if (waitStatus == Node.SIGNAL) {
            return true;
        }
        if (waitStatus > 0) {//取消状态
            for (; ; ) {
                //从右往左一次找 符合SIGNAL状态的等待节点 CANCELED节点直接跳过
                node.prev = pred = pred.prev;
                if (pred.waitStatus > 0) {
                    break;
                }
            }
            pred.next = node;
            return true;
        } else {//判断初始状态为0
            compareAndSetWaitState(pred, waitStatus, Node.SIGNAL);
        }
        return false;
    }

    private boolean compareAndSetWaitState(Node node, int expect, int update) {
        return UNSAFE.compareAndSwapInt(node, waitStateOffset, expect, update);
    }

    private boolean compareAndSetNext(Node pred, Node expect, Object update) {
        return UNSAFE.compareAndSwapObject(pred, nextStateOffset, expect, update);
    }

    private boolean compareAndSetHead(Node update) {
        return UNSAFE.compareAndSwapObject(this, headOffset, null, update);
    }

    private boolean compareAndSetTail(Node expect, Node update) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, expect, update);
    }

    //CAS state
    public boolean compareAndSetState(int expect, int update) {
        return UNSAFE.compareAndSwapInt(this, stateOffset, expect, update);
    }

    private void setHead(Node node) {
        //head 指向成功获取锁的node
        head = node;
        //头节点线程永远锁null
        node.thread = null;
        node.prev = null;
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


    public Node getTail() {
        return tail;
    }

    public void setTail(Node tail) {
        this.tail = tail;
    }
}

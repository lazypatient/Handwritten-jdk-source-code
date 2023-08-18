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
        //尝试快速获取锁 如果失败需要  1.入队 addNode(Node.EXCLUSIVE)  2.自旋+阻塞 acquireQuene
        if (!tryAcquire(args) && acquireQuene(addNode(Node.EXCLUSIVE), args)) {
            //线程走到这里说明 队列阻塞的线程是被中断唤醒的 所以这里要还原中断
            Thread.currentThread().interrupt();

        }

    }

    /**
     * 在尾指针入队 创建新的Node节点 封装线程 CAS修改尾部指针 成功后node节点
     */
    public Node addNode(Node mode) {
        //分两种情况 但都是尾部入队
        //1.第一次入队 需要初始化head tail mode节点
        //2.非第一次入队 调整head tail pred next cas 操作
//        Node node = new Node(Thread.currentThread(), mode);
//        //获取当前队列最后一个节点
//        //快速尝试入队
//        Node pred = tail;
//        if (pred != null) { //这里其实可以优化 jdk9已经进行了优化
//            //新增节点的前节点就是当前队列的最后节点
//            //oldNode<-----newNode
//            node.prev = pred;
//            //CAS后 tail就指向了新增的node节点 tail---->newNode
//            if (compareAndSetTail(pred, node)) {
//                //oldNode---->newNode
//                pred.next = node;
//                return node;
//            }
//        }
//        //走到这里说明  1.队列没有初始化 第一次入队 2.node CAS失败 需要自旋入队
//        casEnqueue(node);
//
//        return node;

        //为了提高可读性，我移除了快速尝试修改tail节点的代码
        //而且在jdk8之后，这块快速尝试也被移除了 可读性太差了 而且增加了代码的复杂程度
        //在某种意义上 这种快速尝试入队 并没有提升多大的性能
        Node node = new Node(Thread.currentThread(), mode);
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
                    return node;
                }
            }
        }
    }

    //此方法被优化了 暂时不用了
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
     * head节点是已经获取锁的节点了，所以再一次去抢锁就是头节点的后继节点
     * 那又为什么非得是头节点的后继节点呢，因为这里的队列是先进先出队列，对尾巴进，对头出，懂了吧
     *
     * @param node 入队的节点
     * @param args 锁状态值 1
     * @return 返回中断信号 false 表明是正常唤醒LockSupport.park ； true表示是非正常唤醒 中断唤醒 true
     */
    public boolean acquireQuene(Node node, int args) {
        boolean failed = true;
        //发生异常处理
        try {
            boolean interrupted = false;
            //死循环 线程一直自获取锁
            for (; ; ) {
                //1.如果前区节点是头节点 则快速尝试获取锁
                Node preNode = node.preNode();
                if (preNode == head && tryAcquire(args)) {
                    //走到这里说明
                    //抢锁成功后，说明旧的头节点已经释放了锁，需要移除队列，在重新设置新的头节点信息
                    setHead(node);
                    //移除oldHead节点和当前head节点的关联
                    preNode.next = null;
                    //没有异常
                    failed = false;
                    //非中断唤醒
                    return interrupted;
                }
                //2.尝试获取锁失败了 说明当前节点不是head的后继节点
                //2.1 shouldParkAfterFailedAcquire
                //检测是否满足阻塞的条件 前驱节点是SINGAL状态 后继节点才可以阻塞；如果找不到则继续向前找

                //2.2 parkAndCheckInterupt 阻塞 LockSupport.park 唤醒有两种方式
                // 正常唤醒：unpark
                // 非正常唤醒： interrrupt（中断），interrrupt后 会立即唤醒阻塞线程 如果不清除中断标记位 那么此线程将无法继续阻塞
                // 所以要中断后要立即清除标记位 业务一致
                if (shouldParkAfterFailedAcquire(preNode, node) && parkAndCheckInterupt()) {
                    interrupted = true;
                }
            }
        } finally {
            //取消 就是把node从对列中摘除
            if (failed) {//这里真的会跨过node节点吗  其实只有next指针跨过了 prev指针并没有跨过
                //prev指针什么时候跨过呢？这两个方法中 由下一个节点对应的线程去完成
                //1.shouldParkAfterFailedAcquire
                //2.cancelAcquire
                //就是这段代码 node.prev = pred = pred.prev;
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
        //前驱节点是SINGAL状态 才可以
        //跳过CANCELLED状态的节点
        while (pred.waitStatus > 0) {
            node.prev = pred = pred.prev;
        }
        //此时已经获取SINGAL的前驱节点了 也就是正常的前驱节点了
        Node predNext = pred.next;
        //设置当前节点状态是CANCELLED
        node.waitStatus = Node.CANCELED;
        //分2种情况
        //1.如果是尾节点 CAS修改
        //？？？？？？？？
        //这么写 我的理解就是找了一次 刚好前驱节点就是当前tail节点 也就是node节点的前一个元素
        //毕竟入队的时候 已经把状态都改成了-1
        if (node == tail && compareAndSetTail(node, pred)) {
            //把尾节点设置为null
            compareAndSetNext(pred, predNext, null);
        }//2.假如该节点是中间节点
        else {
            int ws;
            //2.1 pred不是头节点
            if (pred != head
                    && ((ws = pred.waitStatus) == Node.SIGNAL
                    || (ws <= 0 && compareAndSetWaitState(pred, ws, Node.SIGNAL)))
                    && pred.thread != null) {
                Node next = node.next;
                //跨过当前节点
                if (next != null && next.waitStatus <= 0) {
                    compareAndSetNext(pred, predNext, next);
                }
            } else {
                //2.2 pred是头节点
                //只有头节点的后继节点才有资格去唤醒
                //但是头节点的后继节点 可能也存在很多CANCEL的节点
                //所以要在当前node以后的节点去找SINGAL状态的节点去唤醒
                unparkSuccessor(node);
            }
            node.next = node;
        }
    }

    //唤醒后继节点（通常是头节点去唤醒）
    private void unparkSuccessor(Node node) {
        int waitStatus = node.waitStatus;
        if (waitStatus < 0) {
            //把改节点恢复为初始状态（因为该节点即将删除出队）
            compareAndSetWaitState(node, waitStatus, 0);
        }
        //获取后继节点 从后往前找
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {//CANCELLED
            s = null;
            //找到最靠近头节点的可以唤醒的节点 也就是SINGAL是-1的节点
            for (Node t = tail; t != null && t != node; t = t.prev) {
                if (t.waitStatus <= 0) {
                    s = t;
                }
            }
        }
        if (s != null) {
            LockSupport.unpark(s.thread);//找到并唤醒后继节点
        }
    }


    private boolean parkAndCheckInterupt() {
        //这里唤醒只有两种情况 第一种是正常唤醒 第二种就是中断
        LockSupport.park(this);
        //返回中断标记位 并且重置标记位 不然该节点无法继续阻塞
        return Thread.interrupted();
    }

    /**           --------------------------->
     *            <---------------------------
     *                   CANCELED to skip
     *        +------+  prev +------+       +------+
     *        |      | <---- |      | <---- |      |
     *  head  | node |  next | node |       | node |  tail
     *        |      | ----> |      | ----> |      |
     *        +------+       +------+       +------+
     *            -------------------------->
     *            <--------------------------
     *
     * @param pred 前驱节点
     * @param node 当前尝试获取锁失败的节点
     * @return 满足阻塞的条件 true；不满足 false
     */

    private boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        //获取前驱节点的状态
        int waitStatus = pred.waitStatus;
        //前驱节点的状态是SIGNAL 则当前节点就满足阻塞的条件
        if (waitStatus == Node.SIGNAL) {
            return true;
        }
        if (waitStatus > 0) {//非SIGNAL状态 ；实际CANCELLED 取消状态
            //从右往左依次找前驱节点，如果找到符合SIGNAL状态的前驱节点节点，那么就满足当前节点阻塞条件
            //CANCELLED状态的前驱节点直接跳过
            do {
                //跳过非SIGNAL状态的节点，
                node.prev = pred = pred.prev;
            }
            //直到找到SIGNAL状态的节点，将当前node的前指针指向该找到的节点
            while (pred.waitStatus > 0);

            pred.next = node;
            return true;
        } else {//前驱节点 waitStatus<0 和 >0 都已经处理过了 ；开始讨论waitStatus=0 该如何处理
            //前驱节点 waitStatus = 0的需要立即修改成SINGAL状态 ; 多线程竞争修改 要用CAS操作
            //那么问题来了 为什么这里的节点状态会有0 -1 等情况呢 ？
            //是因为在addNode入队的时候，并没有对node节点的状态做任何的初始化，默认就是0，所以在这里完成了所愿节点的状态初始化
            //同理 既然不满足前驱节点锁SINGAL状态 那就返回false
            //下一轮循环就会用到这个SINGAL状态的节点了
            //这里其实锁一个死循环去处理的
            compareAndSetWaitState(pred, waitStatus, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param arg
     * @return
     */
    public boolean release(int arg) {
        //1.state-1
        if (tryRelease(arg)) {//尝试释放锁
            //2.唤醒头节点的后继节点
            Node h = head;
            if (h != null && h.waitStatus != 0) {
                unparkSuccessor(h);
            }
            return true;
        }

        return false;
    }

    /**
     * 具体实现看各个锁
     *
     * @param arg
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    private boolean compareAndSetWaitState(Node node, int expect, int update) {
        return UNSAFE.compareAndSwapInt(node, waitStateOffset, expect, update);
    }

    private boolean compareAndSetNext(Node pred, Node expect, Node update) {
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

    /**
     * 将自己设置为头节点
     *
     * @param node
     */
    private void setHead(Node node) {
        head = node;
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
        Node t = tail;
        Node h = head;
        Node s;
        return h != tail && ((s = h.next) == null || s.thread != Thread.currentThread());
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

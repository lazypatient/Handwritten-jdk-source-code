package com.leaf.threadworld.aqs;


/*
    AQS = CAS+CLH双向链表+fifo+LockSupport

       +------+  prev +------+       +------+
       |      | <---- |      | <---- |      |
 head  | node |  next | node |       | node |  tail
       |      | ----> |      | ----> |      |
       +------+       +------+       +------+
 */
public abstract class MyAbstractQueuedSynchronizer {


}

## 手写JDK核心源码 ##

💪 手写JDK各大核心包源码，目前专注于手写JUC并发包源码，所有的代码提供了测试案例和详细的注释，掉了我不少头发，给个star✨😂，仅用于学习面试🐶，无任何商用。
### MyAtomicInteger ###

    支持JDK的AtomicInteger的所有功能，并且做了扩展，利用乐观🔒能够解决ABA问题。

### AQS ###
    
    基于ReentrantLock独占锁模式，实现了MyAbstractQueuedSynchronizer（AQS），并手写了MyReentrantLock。

### 线程池 ###

    实现了简易轻量不会OOM的线程池，除了JDK的功能外，还提供了任务队列的动态扩容和缩容（JDK的线程池不支持在使用过程中对任务队列容量进行限制调整）。
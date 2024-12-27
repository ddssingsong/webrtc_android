package com.dds.temple2.socket.utils;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 子线程调度器
 *
 */
public class Dispatcher {
    private static final String TAG = "Dispatcher";

    /*********************** copy form AsyncTask start ***********************/
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static class NameThreadFactory implements ThreadFactory {
        String name;
        private final AtomicInteger mCount = new AtomicInteger(1);

        private NameThreadFactory(String name) {
            this.name = name;
        }

        public static ThreadFactory create(String name) {
            return new NameThreadFactory(name);
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, name + "#" + mCount.getAndIncrement());
        }
    }

    /**
     * An {@link ExecutorService} that can be used to execute tasks in parallel.
     */
    private static final ExecutorService THREAD_POOL_EXECUTOR = newThreadPool("Dispatcher");

    /*********************** copy form AsyncTask end ***********************/

    /**
     * 生产一个线程池
     * <p>
     * 目前2个地方用到：
     * 1. Dispatcher
     * 2. DLManager
     * 3. HttpManager
     * //TODO 4.MQTT线程池 ==>李洋彪
     * 线程池是否需要关闭，请在外部自行关闭
     *
     * @param threadNamePre 线程池name的前缀
     */
    public static ThreadPoolExecutor newThreadPool(String threadNamePre) {
        final BlockingQueue<Runnable> poolWorkQueue = new LinkedBlockingQueue<Runnable>();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                poolWorkQueue, NameThreadFactory.create(threadNamePre));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    private int maxRequests = 64;
    private Runnable idleCallback;

    private ExecutorService executorService; // 可以自定义，默认使用THREAD_POOL_EXECUTOR

    private final Deque<Runnable> readyAsyncCalls = new ArrayDeque<>();

    private final Deque<Runnable> runningAsyncCalls = new ArrayDeque<>();

    private final Deque<Callable> runningSyncCalls = new ArrayDeque<>();

    public static Dispatcher with() {
        return DispatcherHolder.sInstance;
    }

    private static class DispatcherHolder {
        private static final Dispatcher sInstance = new Dispatcher();
    }

    private Dispatcher() {
    }

    /**
     * 设置后，将会在此线程池中调度线程
     *
     * @param executorService 线程池{@link ExecutorService}
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private synchronized ExecutorService executorService() {
        return executorService == null ? THREAD_POOL_EXECUTOR : executorService;
    }

    /**
     * 设置并行运行的线程数量
     *
     * @param maxRequests >1
     */
    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        promoteCalls();
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }


    private void promoteCalls() {
        if (runningCallsCount() >= maxRequests) {
            // Already running max capacity.
            return;
        }
        if (readyAsyncCalls.isEmpty()) {
            // No ready calls to promote.
            return;
        }

        for (Iterator<Runnable> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            Runnable call = i.next();
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
            if (runningCallsCount() >= maxRequests) {
                // Reached max capacity.
                return;
            }
        }
    }

    public void finished(Runnable call) {
        finished(runningAsyncCalls, call, true);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) {
                throw new AssertionError("Call wasn't in-flight!");
            }
            if (promoteCalls) {
                promoteCalls();
            }
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }
}

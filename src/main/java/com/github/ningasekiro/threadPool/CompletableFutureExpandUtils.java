package com.github.ningasekiro.threadPool;

import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * CompletableFuture 扩展工具
 *
 * @author zhangtianci7
 */
public class CompletableFutureExpandUtils {

    /**
     * 如果在给定超时之前未完成，则异常完成此 CompletableFuture 并抛出 {@link TimeoutException} 。
     *
     * @param timeout 在出现 TimeoutException 异常完成之前等待多长时间，以 {@code unit} 为单位
     * @param unit    一个 {@link TimeUnit}，结合 {@code timeout} 参数，表示给定粒度单位的持续时间
     * @return 入参的 CompletableFuture
     */
    public static <T> CompletableFuture<T> orTimeout(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        if (future.isDone()) {
            return future;
        }

        return future.whenComplete(new Canceller(Delayer.delay(new Timeout(future), timeout, unit)));
    }

    /**
     * 超时时异常完成的操作
     */
    static final class Timeout implements Runnable {
        final CompletableFuture<?> future;

        Timeout(CompletableFuture<?> future) {
            this.future = future;
        }

        @Override
        public void run() {
            if (null != future && !future.isDone()) {
                future.completeExceptionally(new TimeoutException());
            }
        }
    }

    /**
     * 取消不需要的超时的操作
     */
    static final class Canceller implements BiConsumer<Object, Throwable> {
        final Future<?> future;

        Canceller(Future<?> future) {
            this.future = future;
        }

        @Override
        public void accept(Object ignore, Throwable ex) {
            if (null == ex && null != future && !future.isDone()) {
                future.cancel(false);
            }
        }
    }

    /**
     * 单例延迟调度器，仅用于启动和取消任务，一个线程就足够
     */
    static final class Delayer {
        static final ScheduledThreadPoolExecutor delayer;

        static {
            delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory());
            delayer.setRemoveOnCancelPolicy(true);
        }

        static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        static final class DaemonThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureExpandUtilsDelayScheduler");
                return t;
            }
        }
    }

//    public static void main(String[] args) {
//        new Thread(() -> {
//            try {
//                test();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//        new Thread(() -> {
//            try {
//                test();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//    }
//
//    public static void test() throws InterruptedException {
//        Thread.sleep(20000);
//        System.out.println("ddddd");
//    }
}

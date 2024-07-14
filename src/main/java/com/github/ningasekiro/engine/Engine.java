package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.exception.AsyncExceptionInterceptor;
import com.github.ningasekiro.exception.AsyncExceptionInterceptorFactory;
import com.github.ningasekiro.task.Task;
import com.github.ningasekiro.task.TaskInput;
import com.github.ningasekiro.threadPool.AsyncThreadPoolFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

@Service
@Lazy
public class Engine {
    @Autowired
    private AsyncThreadPoolFactory asyncThreadPoolFactory;
    @Autowired
    private AsyncExceptionInterceptorFactory asyncExceptionInterceptorFactory;

    private AsyncExceptionInterceptor asyncExceptionInterceptor;

    private ExecutorService executor;


    @PostConstruct
    public void init() {
        this.executor = asyncThreadPoolFactory.getThreadPoolExecutor();
        this.asyncExceptionInterceptor = asyncExceptionInterceptorFactory.getGlobalAsyncExceptionInterceptor();
    }

    public void execute(Message<DAG> message) {
        DAG graph = message.getPayload();
        if (!graph.validate()) {
            throw new RuntimeException("Graph Is Invalid");
        }
        Context context = new Context(message, graph);
        doExecute(graph.getAllNodes(), context);
    }

    private void doExecute(Set<Node> nodes, Context context) {
        Set<Node> processed = context.getProcessed();
        // 选择头节点
        List<Node> canExecuteNodeList = new ArrayList<>();
        for (Node node : nodes) {
            if (!processed.contains(node) && processed.containsAll(node.getParents())) {
                canExecuteNodeList.add(node);
            }
        }
        // 执行头节点
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        long maxTimeout = canExecuteNodeList.stream()
                .mapToLong(taskNode -> taskNode.getTask().getTimeout())
                .max()
                .getAsLong();
        for (Node node : canExecuteNodeList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                taskExecute(node, context);
            }, executor);
            futureList.add(future);
            processed.add(node);
        }

// 创建一个用于捕捉第一个异常的CompletableFuture
        CompletableFuture<Void> anyException = new CompletableFuture<>();

// 为每个任务添加异常处理器
        for (CompletableFuture<Void> future : futureList) {
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    // 如果有异常，完成anyException为异常状态
                    if (!anyException.isDone()) {
                        anyException.completeExceptionally(throwable);
                    }
                }
            });
        }

// 直接监听anyException，它会在第一个异常出现时完成
        anyException.whenComplete((result, exception) -> {
            if (exception != null) {
                // 取消所有任务
                for (CompletableFuture<Void> future : futureList) {
                    if (!future.isCancelled()) {
                        future.cancel(true); // 尝试取消任务，如果任务正在运行则中断线程
                    }
                }

                // 已执行的任务进行回滚
                for (Node taskNode : processed) {
                    Task task = taskNode.getTask();
                    task.rollback(TaskInput.builder().message(context.getMessage()).taskId(taskNode.getId()).build());
                }

                // 全局异常处理
                throw asyncExceptionInterceptor.exception(exception);
            }
        });
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).get(maxTimeout,
                    TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        // 后序节点，递归执行
        for (Node node : canExecuteNodeList) {
            doExecute(node.getChildren(), context);
        }
    }

    private void taskExecute(Node node, Context context) {
        Task task = node.getTask();
        Integer retryTimes = task.getRetryTimes() != null ? task.getRetryTimes() : 0; // 默认为0次重试
        while (retryTimes >= 0) {
            try {
                task.run(node, context);
                task.callback(true, context);
                return; // 成功执行后退出
            } catch (Throwable e) {
                if (retryTimes > 0) {
                    retryTimes--; // 减少剩余重试次数
                    continue; // 重新尝试执行任务
                } else {
                    task.callback(false, context);
                    if (task.isTaskInterrupt()) {
                        throw e;
                    }
                }
            }
        }
    }

}

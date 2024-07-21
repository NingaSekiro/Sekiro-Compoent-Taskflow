package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.exception.AsyncExceptionInterceptor;
import com.github.ningasekiro.exception.GlobalAsyncExceptionInterceptor;
import com.github.ningasekiro.task.Task;
import com.github.ningasekiro.threadPool.CompletableFutureExpandUtils;
import com.github.ningasekiro.util.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import java.util.*;
import java.util.concurrent.*;

@Setter
@Getter
@Slf4j
public class Engine {
    private AsyncExceptionInterceptor asyncExceptionInterceptor =
            Singleton.get(GlobalAsyncExceptionInterceptor.class);
    private ThreadPoolExecutor threadPoolExecutor =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(5);


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
        ConcurrentHashMap<CompletableFuture<Void>, Task> futureTaskMap = new ConcurrentHashMap<>();
        for (Node node : canExecuteNodeList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                taskExecute(node, context);
            }, threadPoolExecutor);
            // 添加超时处理
            future = CompletableFutureExpandUtils.orTimeout(future, node.getTask().getTimeout(), TimeUnit.SECONDS);
            futureTaskMap.put(future, node.getTask());
            futureList.add(future);
            processed.add(node);
        }
        doHandleException(context, futureList, processed, futureTaskMap);
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            // 全局异常处理
            if (e.getCause() instanceof TimeoutException) {
                log.error("task timeout");
            }
            throw asyncExceptionInterceptor.exception(e);
        }

        // 后序节点，递归执行
        for (Node node : canExecuteNodeList) {
            doExecute(node.getChildren(), context);
        }
    }

    private void doHandleException(Context context, List<CompletableFuture<Void>> futureList,
                                   Set<Node> processed,
                                   Map<CompletableFuture<Void>, Task> futureTaskMap) {
        // 创建一个用于捕捉第一个异常的CompletableFuture
        CompletableFuture<Void> anyExceptionFuture = new CompletableFuture<>();

        // 为每个任务添加异常处理器
        for (
                CompletableFuture<Void> future : futureList) {
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    // 如果有异常，完成anyException为异常状态
                    if (!anyExceptionFuture.isDone()) {
                        anyExceptionFuture.completeExceptionally(throwable);
                    }
                }
            });
        }

        // 直接监听anyException，它会在第一个异常出现时完成
        anyExceptionFuture.whenComplete((result, exception) ->
        {
            if (exception != null) {
                // 取消所有任务
                for (CompletableFuture<Void> future : futureList) {
                    // 尝试取消任务，如果任务正在运行则中断线程
                    Task task = futureTaskMap.get(future);
                    if (task.enableCancel()) {
                        log.info("before cancel nodeId:{} future isDone:{}", task.getTaskId(),
                                future.isDone());
                        future.cancel(true);
                        log.info("after cancel nodeId:{} future isDone:{}", task.getTaskId(),
                                future.isDone());
                    }
                }
                // 已执行的任务进行回滚
                for (Node taskNode : processed) {
                    Task task = taskNode.getTask();
                    task.rollback(taskNode, context);
                }
            }
        });
    }

    private void taskExecute(Node node, Context context) {
        Task task = node.getTask();
        // 默认为0次重试
        int retryTimes = task.getRetryTimes() != null ? task.getRetryTimes() : 0;
        while (retryTimes >= 0) {
            try {
                task.run(node, context);
                task.callback(true,node, context);
                return; // 成功执行后退出
            } catch (Throwable e) {
                if (retryTimes > 0) {
                    retryTimes--; // 减少剩余重试次数
                    continue; // 重新尝试执行任务
                } else {
                    retryTimes--; // 减少剩余重试次数
                    task.callback(false,node, context);
                    if (task.isTaskInterrupt()) {
                        throw e;
                    }
                }
            }
        }
    }

}

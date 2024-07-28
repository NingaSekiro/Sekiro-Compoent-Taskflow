package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import com.google.common.util.concurrent.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Setter
@Getter
@Slf4j
public class Engine {
    private final ListeningExecutorService listeningExecutorService =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1);

    public void execute(Message<DAG> message) {
        DAG graph = message.getPayload();
        if (!graph.validate()) {
            throw new RuntimeException("Graph Is Invalid");
        }
        Context context = new Context(message, graph, new ConcurrentHashMap<>());
        for (Node node : graph.getAllNodes()) {
            if (node.getParents().isEmpty()) {
                scheduleNode(node, context, context.getNodeFutureHashMap());
            }
        }
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void scheduleNode(Node node, Context context,
                              Map<Node, ListenableFuture<Void>> futures) {
        DAG graph = context.getMessage().getPayload();
        if (futures.keySet().containsAll(node.getParents())) {
            // No dependencies, start immediately
            log.info("Thread: {},node:{},success", Thread.currentThread().getId(), node.getId());
            ListenableFuture<Void> future = listeningExecutorService.submit(() -> {
                node.getTask().run(node, context);
                return null;
            });
            futures.put(node, future);
            // 超時
            future = Futures.withTimeout(future, Duration.ofSeconds(node.getTask().getTimeout()), scheduledExecutorService);
            addListener(node, future, futures, context);
        } else {
            return;
        }
    }

    private void addListener(Node node, ListenableFuture<Void> future, Map<Node,
            ListenableFuture<Void>> futures, Context context) {
        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                node.getTask().callback(true, node, context);
                for (Node child : node.getChildren()) {
                    if (!futures.containsKey(child)) {
                        scheduleNode(child, context, futures);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                node.getTask().callback(false, node, context);
                //rollback+cancel
                for (Map.Entry<Node, ListenableFuture<Void>> nodeListenableFutureEntry : context.getNodeFutureHashMap().entrySet()) {
                    Node key = nodeListenableFutureEntry.getKey();
                    key.getTask().rollback(key, context);
                    if (node.getTask().enableCancel()) {
                        nodeListenableFutureEntry.getValue().cancel(true);
                    }
                }
                log.info("Thread: {},Task failed for node {}: {}", Thread.currentThread().getId(), node.getId(), t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }
}

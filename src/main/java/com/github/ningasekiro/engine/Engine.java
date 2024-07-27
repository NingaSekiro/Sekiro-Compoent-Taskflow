package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import com.google.common.util.concurrent.*;
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
    private final ListeningExecutorService listeningExecutorService =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public void execute(Message<DAG> message) {
        DAG graph = message.getPayload();
        Context context = new Context(message, graph);
        ConcurrentHashMap<Node, ListenableFuture<Void>> futures = new ConcurrentHashMap<>();
        for (Node node : graph.getAllNodes()) {
            if (node.getParents().isEmpty()) {
                scheduleNode(node, context, futures);
            }
        }
    }

    private void scheduleNode(Node node, Context context, ConcurrentHashMap<Node, ListenableFuture<Void>> futures) {
        DAG graph = context.getMessage().getPayload();
        if (futures.keySet().containsAll(node.getParents())) {
            // No dependencies, start immediately
            log.info("Thread: {},node:{},success", Thread.currentThread().getId(), node.getId());
            ListenableFuture<Void> future = listeningExecutorService.submit(() -> {
                node.getTask().run(node, context);
                return null;
            });
            addListener(node, future, futures, context);
        } else {
            return;
        }
    }

    private void addListener(Node node, ListenableFuture<Void> future, ConcurrentHashMap<Node,
            ListenableFuture<Void>> futures, Context context) {
        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                futures.put(node, future);
                for (Node child : node.getChildren()) {
                    if (!futures.containsKey(child)) {
                        scheduleNode(child, context, futures);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Task failed for node " + node.getId() + ": " + t.getMessage());
            }
        }, listeningExecutorService);
    }
}

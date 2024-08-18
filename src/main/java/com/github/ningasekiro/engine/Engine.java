package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Setter
@Getter
@Slf4j
public class Engine {
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(10);
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
                              Map<Node, Promise<Void>> futures) {
        if (futures.keySet().containsAll(node.getParents())&&isParentsDone(futures, node.getParents())) {
            log.info("Thread: {}, node: {}, success", Thread.currentThread().getId(), node.getId());
            Promise<Void> promise = eventLoopGroup.next().newPromise();
            Future<Void> future = eventLoopGroup.submit(() -> {
                try {
                    node.getTask().run(node, context);
                    promise.setSuccess(null);
                } catch (Exception e) {
                    promise.setFailure(e);
                }
                return null;
            });
            futures.put(node, promise);
            // 使用统一的超时处理机制
            scheduleTimeout(node, promise, future);
            addListener(node, promise, futures, context);
        } else {
            return;
        }
    }

    private void scheduleTimeout(Node node, Promise<Void> promise, Future<Void> future) {
        scheduledExecutorService.schedule(() -> {
            if (!promise.isDone()) {
                log.info("Thread: {}, node: {}, timeout", Thread.currentThread().getId(), node.getId());
                promise.tryFailure(new TimeoutException("Task timed out for node: " + node.getId()));
            }
        }, node.getTask().getTimeout(), TimeUnit.SECONDS);
    }

    private void addListener(Node node, Promise<Void> promise, Map<Node, Promise<Void>> futures,
                             Context context) {
        promise.addListener((FutureListener<Void>) f -> {
            if (f.isSuccess()) {
                node.getTask().callback(true, node, context);
                for (Node child : node.getChildren()) {
                    if (!futures.containsKey(child)) {
                        scheduleNode(child, context, futures);
                    }
                }
            } else {
                Throwable cause = f.cause();
                node.getTask().callback(false, node, context);
                // Rollback + Cancel
                for (Map.Entry<Node, Promise<Void>> nodeFutureEntry : context.getNodeFutureHashMap().entrySet()) {
                    Node key = nodeFutureEntry.getKey();
                    key.getTask().rollback(key, context);
                    if (node.getTask().enableCancel()) {
                        nodeFutureEntry.getValue().cancel(true);
                    }
                }
                log.error("Thread: {}, Task failed for node {}: {}", Thread.currentThread().getId(), node.getId(), cause.getMessage());
            }
        });
    }

    private boolean isParentsDone(Map<Node, Promise<Void>> futures, Set<Node> parents) {
        for (Node parent : parents) {
            if (!futures.get(parent).isDone()) {
                return false;
            }
        }
        return true;
    }
}

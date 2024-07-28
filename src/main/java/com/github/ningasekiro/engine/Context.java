
package com.github.ningasekiro.engine;


import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Data;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


/**
 * state context impl
 *
 * @author NingaSekiro
 * @date 2024/06/14
 */
@Data
public class Context {
    private Message<DAG> message;
    private DAG graph;
    private Set<Node> processed = new HashSet<>();
    private Map<Node, ListenableFuture<Void>> nodeFutureHashMap = new HashMap<>();

    public Context(Message<DAG> message, DAG graph, Map<Node, ListenableFuture<Void>> nodeFutureHashMap) {
        this.message = message;
        this.graph = graph;
        this.nodeFutureHashMap = nodeFutureHashMap;
    }

    public Set<String> getAllNodeId() {
        Set<String> processedIds = new HashSet<>();
        for (Node node : processed) {
            processedIds.add(node.getId());
        }
        return processedIds;
    }
}


package com.github.ningasekiro.engine;


import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.Node;
import lombok.Data;
import org.springframework.messaging.Message;

import java.util.HashSet;
import java.util.Set;


/**
 * state context impl
 *
 * @author NingaSekiro
 * @date 2024/06/14
 */
@Data
public class Context {
    private Message message;
    private DAG graph;
    private Set<Node> processed = new HashSet<>();

    public Context(Message message, DAG graph) {
        this.message = message;
        this.graph = graph;
    }

    public Set<String> getAllNodeId() {
        Set<String> processedIds = new HashSet<>();
        for (Node node : processed) {
            processedIds.add(node.getId());
        }
        return processedIds;
    }
}

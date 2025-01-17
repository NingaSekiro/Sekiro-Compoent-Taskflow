package com.github.ningasekiro.dag;


import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DefaultDAG implements DAG {

    private final Set<Node> nodes = new HashSet<>();
    private final String id;

    public DefaultDAG() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public void addEdge(Node from, Node to) {
        if (from.equals(to)) {
            return;
        }
        from.addChildren(to);
        to.addParent(from);
        nodes.add(from);
        nodes.add(to);
    }

    @Override
    public Set<Node> getAllNodes() {
        return nodes;
    }

    @Override
    public boolean validate() {
        Node root = null;
        for (Node node : nodes) {
            if (node.getParents().isEmpty()) {
                root = node;
                break;
            }
        }
        if (root == null) {
            return false;
        }

        Map<String, Node> visited = new HashMap<>();
        return dfs(root, visited);
    }

    private boolean dfs(Node root, Map<String, Node> visited) {
        if (root == null) {
            return true;
        }

        visited.put(root.getId(), root);
        for (Node child : root.getChildren()) {
            if (visited.containsKey(child.getId())) {
                log.warn("Node: {} is circled", child.getId());
                return false;
            }

            visited.put(child.getId(), child);
            if (!dfs(child, visited)) {
                return false;
            }
        }
        visited.remove(root.getId());

        return true;
    }

    @Override
    public String getId() {
        return id;
    }
}

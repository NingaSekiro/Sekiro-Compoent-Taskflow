package com.github.ningasekiro.dag;

import java.util.Set;

public interface DAG{
    void addEdge(Node from, Node to);
    Set<Node> getAllNodes();

    boolean validate();

    String getId();
}

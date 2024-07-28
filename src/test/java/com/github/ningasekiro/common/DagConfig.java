package com.github.ningasekiro.common;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.DefaultDAG;
import com.github.ningasekiro.dag.DefaultNode;
import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.task.Task;

public class DagConfig {
    public static DAG getDag( Task task ) {
        DAG graph = new DefaultDAG();

        Node nodeA = new DefaultNode("A", task);
        Node nodeB = new DefaultNode("B", task);
        Node nodeC = new DefaultNode("C", task);
        Node nodeD = new DefaultNode("D", task);
        Node nodeE = new DefaultNode("E", task);
        Node nodeF = new DefaultNode("F", task);

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);
        return graph;
    }

    public static DAG getTwoDag( Task task ) {
        DAG graph = new DefaultDAG();

        Node nodeA = new DefaultNode("A", task);
        Node nodeB = new DefaultNode("B", task);
        Node nodeC = new DefaultNode("C", task);
        Node nodeD = new DefaultNode("D", task);
        Node nodeE = new DefaultNode("E", task);
        Node nodeF = new DefaultNode("F", task);

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeF);
        return graph;
    }
}

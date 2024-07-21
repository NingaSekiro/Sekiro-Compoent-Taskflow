package com.github.ningasekiro.engine;

import com.github.ningasekiro.Context;
import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.dag.DefaultDAG;
import com.github.ningasekiro.dag.DefaultNode;
import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.PrintTask;
import com.github.ningasekiro.util.Singleton;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;


public class EngineTest {
    Engine engine = Singleton.get(Engine.class);

    // 已支持: 停止任务, 超時，回滾，重試
    // TODO: 属性配置
    @Test
    public void testExecute() {
        DAG graph = new DefaultDAG();

        PrintTask printTask = new PrintTask();
        Node nodeA = new DefaultNode("A", printTask);
        Node nodeB = new DefaultNode("B", printTask);
        Node nodeC = new DefaultNode("C", printTask);
        Node nodeD = new DefaultNode("D", printTask);
        Node nodeE = new DefaultNode("E", printTask);
        Node nodeF = new DefaultNode("F", printTask);

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new Context()).build();
        engine.execute(message);
    }
}



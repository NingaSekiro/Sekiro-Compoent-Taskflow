package com.github.ningasekiro.engine;

import com.github.ningasekiro.common.DagConfig;
import com.github.ningasekiro.common.TestContext;
import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.task.PrintCallbackTask;
import com.github.ningasekiro.task.PrintTask;
import com.github.ningasekiro.util.Singleton;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;


public class TaskTest {
    Engine engine = Singleton.get(Engine.class);

    // 已支持: 停止任务, 超時，回滾，重試
    // TODO: 属性配置
    @Test
    public void testExecute() {
        DAG graph = DagConfig.getDag(new PrintTask());
        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new TestContext()).build();
        engine.execute(message);
    }

    @Test
    public void testCallbackExecute() {
        DAG graph = DagConfig.getDag(new PrintCallbackTask());
        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new TestContext()).build();
        engine.execute(message);
    }
}



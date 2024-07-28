package com.github.ningasekiro.engine;

import com.github.ningasekiro.dag.DAG;
import com.github.ningasekiro.common.DagConfig;
import com.github.ningasekiro.task.*;
import com.github.ningasekiro.common.TestContext;
import com.github.ningasekiro.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskExceptionTest {
    Engine engine = Singleton.get(Engine.class);

    @Test
    public void testCancelTask() {
        DAG graph = DagConfig.getTwoDag(new PrintExceptionCancelTask());
        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new TestContext()).build();
        engine.execute(message);
    }

    @Test
    public void testTimeOutTask() {
        DAG graph = DagConfig.getDag(new PrintTimeOutTask());
        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new TestContext()).build();
        engine.execute(message);
    }

    @Test
    public void testRollbackTask() {
        DAG graph = DagConfig.getDag(new PrintRollbackTask());
        Message<DAG> message =
                MessageBuilder.withPayload(graph).setHeader("context",
                        new TestContext()).build();
        engine.execute(message);
    }
}

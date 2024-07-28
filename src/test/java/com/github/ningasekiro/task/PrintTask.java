package com.github.ningasekiro.task;

import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.engine.Context;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintTask implements Task {
    private static final String TASK_NAME = "PrintTask";

    @Override
    public void run(Node node, Context context) {
        log.info("Thread: {} TaskName: {},NodeName: {},processed Node: {}",
                Thread.currentThread().getId(),
                TASK_NAME, node.getId(), context.getAllNodeId());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
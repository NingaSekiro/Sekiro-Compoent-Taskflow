package com.github.ningasekiro.task;

import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.engine.Context;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintNotInterruptTask implements Task {
    private static final String TASK_NAME = "PrintTask";

    @Override
    public void run(Node node, Context context) {
        log.info("Thread: {} TaskName: {},NodeName: {},processed Node: {}",
                Thread.currentThread().getId(),
                TASK_NAME, node.getId(), context.getAllNodeId());
        try {
            Thread.sleep(100);
            if (node.getId().equals("B")) {
                int i = 1 / 0;
            }
            if (node.getId().equals("D")) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isTaskInterrupt() {
        return false;
    }
}
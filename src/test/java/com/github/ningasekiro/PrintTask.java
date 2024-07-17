package com.github.ningasekiro;

import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.engine.Context;
import com.github.ningasekiro.task.Task;

public class PrintTask implements Task {
    private static final String TASK_NAME = "PrintTask";

    public PrintTask() {
    }

    @Override
    public void run(Node node, Context context) {
        // LOGGER.info("Task: {}-{} Input: {} Output: {}", taskName, taskId, input.getParameters(), true);
        try {
            Thread.sleep(1000);

//        System.out.printf("Thread: %s Task: %s-%s Output: %s\n", Thread.currentThread().getId(), getTaskName(), taskId, true);
//        Map<String, String> output = new LinkedHashMap<>();
//        output.put(taskId + "_Result", Boolean.toString(true));
            if (node.getId().equals("B")) {
                int i = 1 / 0;
            }
            if (node.getId().equals("D")) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void callback(boolean success, Context context) {
    }
}
package com.github.ningasekiro.task;

import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.engine.Context;

public interface Task {
    void run(Node node, Context input);

    default String getTaskId() {
        return this.getClass().getName();
    }
    default boolean isTaskInterrupt() {
        return true;
    }

    /**
     * 超时时间 默认10s
     */
    default Long getTimeout() {
        return 10L;
    }

    default Integer getRetryTimes() {
        return 1;
    }

    default void callback(boolean success, Context context) {
    }

    default void rollback(Node node, Context input) {
        return;
    }
}


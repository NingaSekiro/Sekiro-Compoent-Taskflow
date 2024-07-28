package com.github.ningasekiro.task;

import com.github.ningasekiro.dag.Node;
import com.github.ningasekiro.engine.Context;

import java.util.concurrent.Callable;

public interface Task{
    void run(Node node, Context input);

    default String getTaskId() {
        return this.getClass().getName();
    }

    /**
     * 超时时间 默认24h
     */
    default Long getTimeout() {
        return 24*60*60L;
    }

    default void callback(boolean success, Node node, Context context) {
    }

    default void rollback(Node node, Context input) {
        return;
    }

    default boolean enableCancel() {
        return false;
    }
}


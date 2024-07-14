package com.github.ningasekiro.dag;


import com.github.ningasekiro.task.Task;

import java.util.Set;

public interface Node {
    // build children and parent
    Set<Node> getChildren();
    Set<Node> getParents();
    void addChildren(Node child);
    void addParent(Node parent);

    String getId();

    void setId(String id);

    void setTask(Task task);

    Task getTask();
}

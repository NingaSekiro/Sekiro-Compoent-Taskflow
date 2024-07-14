package com.github.ningasekiro.dag;

import com.github.ningasekiro.task.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class DefaultNode implements Node {
    private final Set<Node> parents = new HashSet<>();
    private final Set<Node> children = new HashSet<>();

    private String id;
    private Task task;
    public DefaultNode(Task task) {
        this.task = task;
        this.id = task.getTaskId();
    }

    public DefaultNode(String id, Task task) {
        this.id = id;
        this.task = task;
    }

    @Override
    public void addChildren(Node child) {
        children.add(child);
    }

    @Override
    public void addParent(Node parent) {
        parents.add(parent);
    }
}

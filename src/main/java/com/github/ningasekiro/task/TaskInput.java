package com.github.ningasekiro.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.Message;

import java.util.Map;

@Getter
@Setter
@Builder
public class TaskInput {
    private String taskId;
    private Message message;
}

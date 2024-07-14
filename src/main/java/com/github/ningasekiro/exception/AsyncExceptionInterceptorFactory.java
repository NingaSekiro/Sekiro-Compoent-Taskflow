package com.github.ningasekiro.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AsyncExceptionInterceptorFactory {
    @Autowired
    private GlobalAsyncExceptionInterceptor globalAsyncExceptionInterceptor;
}
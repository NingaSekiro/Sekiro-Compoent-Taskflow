package com.github.ningasekiro.exception;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;

@Component
public class GlobalAsyncExceptionInterceptor implements AsyncExceptionInterceptor {

    @Override
    public CompletionException exception(Throwable throwable) {
        return new CompletionException(throwable);
    }
}
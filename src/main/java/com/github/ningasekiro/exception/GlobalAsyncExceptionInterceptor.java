package com.github.ningasekiro.exception;


import java.util.concurrent.CompletionException;

public class GlobalAsyncExceptionInterceptor implements AsyncExceptionInterceptor {

    @Override
    public CompletionException exception(Throwable throwable) {
        return new CompletionException(throwable);
    }
}
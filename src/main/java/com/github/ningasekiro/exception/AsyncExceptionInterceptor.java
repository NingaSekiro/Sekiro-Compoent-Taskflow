package com.github.ningasekiro.exception;

import java.util.concurrent.CompletionException;

public interface AsyncExceptionInterceptor {
    CompletionException exception(Throwable throwable);
}
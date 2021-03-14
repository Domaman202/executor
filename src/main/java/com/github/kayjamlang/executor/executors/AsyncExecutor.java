package com.github.kayjamlang.executor.executors;

import com.github.kayjamlang.core.containers.ThreadContainer;
import com.github.kayjamlang.core.provider.Context;
import com.github.kayjamlang.core.provider.ExpressionProvider;
import com.github.kayjamlang.core.provider.MainExpressionProvider;

import java.util.concurrent.CompletableFuture;

public class AsyncExecutor extends ExpressionProvider<ThreadContainer, Object> {
    @Override
    public Object provide(MainExpressionProvider<Object> mainProvider,
                          Context context,
                          Context argsContext,
                          ThreadContainer expression) {
        CompletableFuture.runAsync(() -> {
            try {
                mainProvider.provide(expression, context, context);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        return null;
    }
}
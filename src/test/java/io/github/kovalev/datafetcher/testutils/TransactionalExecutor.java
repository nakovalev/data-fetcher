package io.github.kovalev.datafetcher.testutils;

import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

public class TransactionalExecutor {

    @Transactional(propagation = REQUIRES_NEW)
    public void executeWithInNewTransaction(Runnable function) {
        function.run();
    }

    @Transactional(propagation = REQUIRES_NEW)
    public <T> T executeWithInNewTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

}

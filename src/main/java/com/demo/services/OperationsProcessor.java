package com.demo.services;

import com.demo.to.Operation;
import com.demo.to.QueueState;
import rx.Observable;

public interface OperationsProcessor {

    Observable<Operation> rawOperationStream();

    Observable<Operation> windowedOperationStream(int offset, int limit);

    Observable<QueueState> queueStateStream();
}

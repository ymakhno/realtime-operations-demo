package com.demo.services.impl;

import com.demo.dao.OperationsDao;
import com.demo.to.Operation;
import com.demo.to.QueueState;
import com.demo.services.OperationsProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OperationsProcessorImpl implements OperationsProcessor {

    private static final long MILLISECONDS_IN_MINUTE = 60000;

    private final OperationsDao operationsDao;

    private final PublishSubject<Operation> operationSubject;
    private final BehaviorSubject<QueueState> queueStateSubject;
    private final OperationsQueue queue;

    private volatile long lastReadRow;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();;

    @Autowired
    public OperationsProcessorImpl(OperationsDao operationsDao,
                                   @Value("${operations.timeframe:60}") int timeframeMinutes) {
        this.operationsDao = operationsDao;
        this.queue = new OperationsQueue(timeframeMinutes * MILLISECONDS_IN_MINUTE);
        this.operationSubject = PublishSubject.create();
        this.queueStateSubject = BehaviorSubject.create(new QueueState(-1, 0));
        executorService.scheduleAtFixedRate(this::processNewOperations, 1000, 500, TimeUnit.MILLISECONDS);
    }

    @PostConstruct
    protected synchronized void initialRead() {
        long current = System.currentTimeMillis();
        List<Operation> operations =
            operationsDao.findOperationsBetweenDates(new Date(current - queue.getTimeframe()), new Date(current));
        Collections.sort(operations, (o1, o2) -> (int) Math.signum(o1.getRowId() - o2.getRowId()));

        queue.addNewOperations(operations);
        if (!operations.isEmpty()) {
            queueStateSubject.onNext(queue.getCurrentState());
        }
    }

    @Scheduled(fixedRate = 500)
    protected synchronized void processNewOperations() {
        long current = System.currentTimeMillis();
        List<Operation> operations = operationsDao.findOperations(lastReadRow);
        Collections.sort(operations, (o1, o2) -> (int) Math.signum(o1.getRowId() - o2.getRowId()));

        queue.addNewOperations(operations);
        queue.removeOldOperations();

        queueStateSubject.onNext(queue.getCurrentState());
        operations.forEach(operationSubject::onNext);
        if (!operations.isEmpty()) {
            lastReadRow = operations.get(operations.size() - 1).getRowId();
        }
        if (log.isDebugEnabled()) {
            log.debug("Observers: " + operationSubject.hasObservers() +
                " Took: " + (System.currentTimeMillis() - current) + " scheduled: " +
                new SimpleDateFormat("mm:ss.SSS").format(current));
        }
    }

    @Override
    public Observable<Operation> rawOperationStream() {
        return operationSubject;
    }

    @Override
    public Observable<QueueState> queueStateStream() {
        return queueStateSubject;
    }

    @Override
    public Observable<Operation> windowedOperationStream(int offset, int limit) {
        return Observable.defer(() -> generateStream(offset, limit))
                .withLatestFrom(queueStateStream(),
                    (o, state) -> (o != null && o.getRowId() >= state.getLastRowInQueue()) ? o : null)
                .filter(o -> o != null)
                .publish()
                .refCount();
    }

    private synchronized Observable<Operation> generateStream(int offset, int limit) {
        ReplaySubject<Operation> replaySubject = ReplaySubject.createWithSize(offset + 25);
        Subscription subscription = operationSubject.subscribe(replaySubject);

        List<Operation> initialData = queue.copyFromTheEnd(offset + limit);
        Observable<Operation> emptyCells = initialData.size() < offset + limit ?
            Observable.range(0, offset + limit - initialData.size()).map(i -> null) :
            Observable.empty();
        Observable<Operation> windowedOperationsStream = Observable.concat(
            emptyCells,
            Observable.from(initialData),
            replaySubject
        );

        Observable<Void> emitOrderStream = Observable.concat(
            Observable.range(0, limit).map(a -> null),
            replaySubject.map(a -> null)
        );
        return windowedOperationsStream
            .zipWith(emitOrderStream, (o, v) -> o)
            .doOnUnsubscribe(subscription::unsubscribe);
    }
}

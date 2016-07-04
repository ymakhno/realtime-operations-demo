package com.demo.web.websocket;

import com.demo.services.OperationsProcessor;
import com.demo.to.QueueState;
import com.demo.web.websocket.protocol.Event;
import com.demo.web.websocket.protocol.StartStreamCommand;
import com.demo.to.Operation;
import com.demo.web.websocket.protocol.DataEvent;
import com.demo.web.websocket.protocol.ErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class StreamHandler {

    private final OperationsProcessor operationsProcessor;
    private final StartStreamCommand configuration;

    private volatile Subscription subscription;
    private volatile DataEvent latestEvent;

    public StreamHandler(StartStreamCommand configuration, OperationsProcessor operationsProcessor) {
        this.configuration = configuration;
        this.operationsProcessor = operationsProcessor;
    }

    public String getStreamId() {
        return configuration.getStreamId();
    }

    public void startStream(Consumer<Event> eventConsumer) {
        subscription = operationsProcessor.windowedOperationStream(configuration.getOffset(), configuration.getLimit())
            .buffer(200, TimeUnit.MILLISECONDS, configuration.getLimit())
            .map(this::cleanUnnecessaryFields)
            .withLatestFrom(operationsProcessor.queueStateStream(), this::toDataEvent)
            .observeOn(Schedulers.io())
            .filter(Optional::isPresent)
            .subscribe(
                e -> eventConsumer.accept(e.get()),
                ex -> {
                    log.warn("Request processing error", ex);
                    eventConsumer.accept(new ErrorEvent(getStreamId(), ex.getMessage()));
                }
            );
    }

    public void stopStream() {
        if (subscription != null && ! subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            log.warn("StreamHandler " + getStreamId() + " is already closed");
        }
    }

    private List<Map<String, Object>> cleanUnnecessaryFields(List<Operation> operation) {
        List<Map<String, Object>> result = operation.stream()
            .map(this::cleanUnnecessaryFields)
            .collect(Collectors.toList());
        Collections.reverse(result);
        return result;
    }

    private Map<String, Object> cleanUnnecessaryFields(Operation operation) {
        Set<String> fields = configuration.getRequestedFields().get(operation.getType());
        Map<String, Object> result = new LinkedHashMap<>();
        operation.getProperties().entrySet().stream()
            .filter(e -> fields.contains(e.getKey()))
            .forEach(e -> result.put(e.getKey(), e.getValue()));
        result.put("rowId", operation.getRowId());
        result.put("type", operation.getType());
        result.put("created", operation.getCreated());
        return result;
    }

    private Optional<DataEvent> toDataEvent(List<Map<String, Object>> newRows, QueueState queueState) {
        if (newRows.isEmpty() && queueState.equals(latestEvent.getQueueState())) {
            return Optional.empty();
        }
        DataEvent newEvent = new DataEvent(getStreamId(), newRows, queueState);
        latestEvent = newEvent;
        return Optional.of(newEvent);
    }
}

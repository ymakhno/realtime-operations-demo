package com.demo.services.impl;

import com.demo.to.Operation;
import com.demo.to.QueueState;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OperationsQueue {

    private final long timeframe;
    private final LinkedList<Operation> operations = new LinkedList<>();

    public OperationsQueue(long timeframe) {
        this.timeframe = timeframe;
    }

    public long getTimeframe() {
        return timeframe;
    }

    public QueueState getCurrentState() {
        return new QueueState(
            operations.isEmpty() ? -1L : operations.getFirst().getRowId(),
            operations.size()
        );
    }

    public void addNewOperations(List<Operation> newOperations) {
        operations.addAll(newOperations);
    }

    public Operation removeOldOperations() {
        Date timeBoundary = new Date(System.currentTimeMillis() - timeframe);
        Iterator<Operation> iterator = operations.listIterator();
        while (iterator.hasNext()) {
            Operation operation = iterator.next();
            if (operation.getCreated().before(timeBoundary)) {
                iterator.remove();
            } else {
                return operation;
            }
        }
        return null;
    }

    public List<Operation> copyFromTheEnd(int items) {
        int from = Math.max(0, operations.size() - items);
        List<Operation> subList = from == 0 ? operations : operations.subList(from, operations.size());
        return new ArrayList<>(subList);
    }
}

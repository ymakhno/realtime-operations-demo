package com.demo.dao;

import com.demo.to.Operation;

import java.util.Date;
import java.util.List;

public interface OperationsDao {

    List<Operation> findOperations(long fromSeqNumber);

    List<Operation> findOperationsBetweenDates(Date from, Date to);
}

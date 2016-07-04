package com.demo.dao.orientdb;

import com.demo.dao.OperationsDao;
import com.demo.to.Operation;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.demo.to.OperationType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OperationsDaoImpl implements OperationsDao {
    public static final String OPERATION_COLLECTION = "OPERATION";

    private final ObjectProvider<ODatabaseDocumentTx> dbProvider;

    @Autowired
    public OperationsDaoImpl(ObjectProvider<ODatabaseDocumentTx> dbProvider) {
        this.dbProvider = dbProvider;
    }

    @Override
    public List<Operation> findOperations(long fromSeqNumber) {
        try (ODatabaseDocumentTx db = dbProvider.getObject()) {
            List<ODocument> documents = db.query(new OSQLSynchQuery<>(
                "select * from " + OPERATION_COLLECTION + " where rowId > ?"
            ), fromSeqNumber);

            return toOperation(documents);
        }
    }

    @Override
    public List<Operation> findOperationsBetweenDates(Date from, Date to) {
        try (ODatabaseDocumentTx db = dbProvider.getObject()) {
            List<ODocument> documents = db.query(new OSQLSynchQuery<>(
                "select * from " + OPERATION_COLLECTION + " where created >= ? and created <= ? order by created"
            ), from, to);

            return toOperation(documents);
        }
    }

    private List<Operation> toOperation(List<ODocument> documents) {
        return documents.stream()
            .map(this::toOperation)
            .collect(Collectors.toList());
    }

    private Operation toOperation(ODocument document) {
        Operation operation = new Operation();
        operation.setRowId(document.field("rowId"));
        operation.setType(OperationType.valueOf(document.field("type")));
        operation.setCreated(document.field("created"));
        Map<String, Object> properties = new HashMap<>();

        Stream.of(document.fieldNames())
            .filter(n -> !("created".equals(n) || "rowId".equals(n)))
            .forEach(n -> properties.put(n, document.field(n)));
        operation.setProperties(properties);
        return operation;
    }
}

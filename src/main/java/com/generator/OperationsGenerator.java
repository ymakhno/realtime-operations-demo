package com.generator;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.sequence.OSequence;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.demo.services.FieldsService;
import com.demo.to.FieldDescription;
import com.demo.to.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.demo.dao.orientdb.OperationsDaoImpl.OPERATION_COLLECTION;

@Slf4j
public class OperationsGenerator {

    private static final int DAY_PERIOD = 24 * 60 * 60 * 1000;
    private static final int MILLISECONDS_IN_MINUTE = 60000;
    private static final int MILLISECONDS_IN_SECOND = 1000;

    private static final String OPERATIONS_SEQUENCE = "operations_seq";

    private final ObjectProvider<ODatabaseDocumentTx> dbProvider;
    private final FieldsService fieldsService;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();

    @Value("${orientdb.generator.rate}")
    private int generatorRate;

    @Value("${orientdb.generator.pregen}")
    private boolean pregen;

    @Value("${operations.timeframe}")
    private int timeframe;

    @Autowired
    public OperationsGenerator(ObjectProvider<ODatabaseDocumentTx> dbProvider, FieldsService fieldsService) {
        this.dbProvider = dbProvider;
        this.fieldsService = fieldsService;
    }

    @PostConstruct
    public void init() {
        executorService.scheduleAtFixedRate(this::generateOperation, generatorRate, generatorRate, TimeUnit.SECONDS);
        try (ODatabaseDocumentTx db = dbProvider.getObject()) {
            OSequence sequence = db.getMetadata().getSequenceLibrary().createSequence(
                OPERATIONS_SEQUENCE,
                OSequence.SEQUENCE_TYPE.ORDERED,
                new OSequence.CreateParams().setStart(0L).setIncrement(1)
            );
            if (pregen) {
                long end = System.currentTimeMillis();
                long start = end - timeframe * MILLISECONDS_IN_MINUTE;
                long step = generatorRate * MILLISECONDS_IN_SECOND;

                for (long i = start; i < end; i += step) {
                    db.save(generateDocument(new Date(i), sequence.next()));
                }
            }

        }
    }

    @PreDestroy
    protected void shutdown() {
        executorService.shutdownNow();
    }

    private void generateOperation() {
        try {
            int timeout = random.nextInt(1000);
            Thread.sleep(timeout);

            OperationType type = OperationType.values()[random.nextInt(OperationType.values().length)];
            try (ODatabaseDocumentTx db = dbProvider.getObject()) {
                OSequence sequence = db.getMetadata().getSequenceLibrary().getSequence(OPERATIONS_SEQUENCE);

                db.save(generateDocument(new Date(), sequence.next()));
                log.debug("New document with type {} generated", type);
            }
        } catch (InterruptedException e) {
            log.info("Task generator was interrupted. Maybe spring context is closing");
        }
    }

    private ODocument generateDocument(Date created, long rowId) {
        OperationType type = OperationType.values()[random.nextInt(OperationType.values().length)];
        ODocument document = new ODocument(OPERATION_COLLECTION);
        fieldsService.getFields(type).forEach(f -> generateField(f, document));

        document.field("rowId", rowId);
        document.field("type", type.toString());
        document.field("created", created);
        return document;
    }

    private void generateField(FieldDescription field, ODocument document) {
        switch (field.getType()) {
            case STRING:
                document.field(field.getName(), generateString());
                break;
            case INTEGER:
                document.field(field.getName(), generateInteger());
                break;
            case TIMESTAMP:
                document.field(field.getName(), generateTimestamp());
                break;
            case BOOLEAN:
                document.field(field.getName(), generateBoolean());
                break;
        }
    }

    private String generateString() {
        int length = 1 + random.nextInt(10);
        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = (char) ('a' + random.nextInt(26));
        }
        return String.valueOf(array);
    }

    private int generateInteger() {
        return random.nextInt(100000);
    }

    private Date generateTimestamp() {
        int periodLocation = random.nextInt(DAY_PERIOD);
        return new Date(System.currentTimeMillis() - periodLocation);
    }

    private boolean generateBoolean() {
        return random.nextInt(2) == 0;
    }
}

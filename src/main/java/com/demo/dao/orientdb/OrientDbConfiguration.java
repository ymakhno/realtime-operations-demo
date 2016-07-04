package com.demo.dao.orientdb;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan(basePackageClasses = OrientDbConfiguration.class)
@ConditionalOnProperty(value = "db.type", havingValue = "orientdb")
public class OrientDbConfiguration {

    @Bean
    public OPartitionedDatabasePool databasePool() {
        OPartitionedDatabasePool pool = new OPartitionedDatabasePool("memory:playtech_demo", "admin", "admin");
        pool.setAutoCreate(true);
        try (ODatabaseDocumentTx db = pool.acquire()) {
            OSchema schema = db.getMetadata().getSchema();
            OIndexManager indexManager = db.getMetadata().getIndexManager();
            if (!schema.existsClass(OperationsDaoImpl.OPERATION_COLLECTION)) {
                schema.createClass(OperationsDaoImpl.OPERATION_COLLECTION);
            }
            if (!indexManager.existsIndex("operation_rowid_index")) {
                OPropertyIndexDefinition indexDefinition = new OPropertyIndexDefinition(
                    OperationsDaoImpl.OPERATION_COLLECTION, "rowId", OType.LONG
                );
                indexManager.createIndex("operation_rowid_index", "UNIQUE", indexDefinition, null, null, null);
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(pool::close));
        return pool;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ODatabaseDocumentTx database() {
        return databasePool().acquire();
    }
}

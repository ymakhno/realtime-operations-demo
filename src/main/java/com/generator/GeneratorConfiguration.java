package com.generator;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.demo.services.FieldsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "orientdb.generator", havingValue = "true")
public class GeneratorConfiguration {

    @Bean
    public OperationsGenerator operationsGenerator(ObjectProvider<ODatabaseDocumentTx> dbProvider,
                                                   FieldsService fieldsService) {
        return new OperationsGenerator(dbProvider, fieldsService);
    }
}

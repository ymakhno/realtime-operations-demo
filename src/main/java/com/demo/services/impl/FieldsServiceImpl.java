package com.demo.services.impl;

import com.demo.services.FieldsService;
import com.demo.to.FieldDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.demo.to.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FieldsServiceImpl implements FieldsService {

    private final Map<OperationType, List<FieldDescription>> fields;
    private final List<FieldDescription> selectedByDefault;

    @Autowired
    public FieldsServiceImpl(ObjectMapper objectMapper) {
        // Loading fields information for different types of operations from schema.json
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.json")) {
            Map<OperationType, List<FieldDescription>> result = Stream.of(OperationType.values())
                .collect(Collectors.toMap(Function.identity(), t -> new ArrayList<>()));
            List<FieldDescription> defaultFields = new ArrayList<>();

            Map<String, List<FieldDescription>> allFields =
                objectMapper.readValue(input, new TypeReference<Map<String, List<FieldDescription>>>(){});

            for (Map.Entry<String, List<FieldDescription>> entry : allFields.entrySet()) {
                List<FieldDescription> fields = entry.getValue();
                String key = entry.getKey();
                if ("common".equals(key)) {
                    result.entrySet().forEach(e -> e.getValue().addAll(fields));
                } else {
                    result.get(OperationType.valueOfIgnoreCase(key)).addAll(fields);
                }
                defaultFields.addAll(fields.size() > 2 ? fields.subList(0, 2) : fields);
            }
            result = result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Collections.unmodifiableList(e.getValue())));
            this.fields = Collections.unmodifiableMap(result);
            this.selectedByDefault = Collections.unmodifiableList(defaultFields);
        } catch (IOException e) {
            log.error("Can't read fields schema", e);
            throw new IllegalStateException("Can't read fields schema");
        }
    }

    @Override
    public List<FieldDescription> getFields(OperationType operationType) {
        return fields.get(operationType);
    }

    @Override
    public List<FieldDescription> getSelectedByDefault() {
        return selectedByDefault;
    }
}

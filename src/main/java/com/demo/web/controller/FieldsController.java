package com.demo.web.controller;

import com.demo.to.FieldDescription;
import com.demo.to.FieldsPayload;
import com.demo.services.FieldsService;
import com.demo.to.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/fields")
public class FieldsController {

    private final FieldsService fieldsService;

    @Autowired
    public FieldsController(FieldsService fieldsService) {
        this.fieldsService = fieldsService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public FieldsPayload getFields() {
        Map<OperationType, List<FieldDescription>> fields = Stream.of(OperationType.values())
            .collect(Collectors.toMap(t -> t, fieldsService::getFields));
        return new FieldsPayload(fields, fieldsService.getSelectedByDefault());
    }
}

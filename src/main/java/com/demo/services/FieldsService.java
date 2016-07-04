package com.demo.services;

import com.demo.to.FieldDescription;
import com.demo.to.OperationType;

import java.util.List;

public interface FieldsService {

    List<FieldDescription> getFields(OperationType operationType);

    List<FieldDescription> getSelectedByDefault();
}

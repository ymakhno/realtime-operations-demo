package com.demo.to;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldsPayload {

    Map<OperationType, List<FieldDescription>> fields;
    List<FieldDescription> defaultFields;
}

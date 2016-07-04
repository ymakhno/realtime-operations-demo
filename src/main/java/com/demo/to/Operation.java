package com.demo.to;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@EqualsAndHashCode
@Getter
@Setter
public class Operation {

    private long rowId;
    private OperationType type;
    private Date created;

    private Map<String, Object> properties;
}

package com.demo.web.websocket.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DataEvent.class, name = "data"),
    @JsonSubTypes.Type(value = ErrorEvent.class, name = "error")
})
public abstract class Event {
}

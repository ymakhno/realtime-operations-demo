package com.demo.web.websocket.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StartStreamCommand.class, name = "startStream"),
    @JsonSubTypes.Type(value = StopStreamCommand.class, name = "stopStream")
})
@Getter
@Setter
@NoArgsConstructor
public abstract class Command {

    protected String streamId;
}

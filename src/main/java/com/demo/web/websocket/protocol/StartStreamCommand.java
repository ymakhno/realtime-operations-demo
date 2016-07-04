package com.demo.web.websocket.protocol;

import com.demo.to.OperationType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class StartStreamCommand extends Command {

    private int offset;
    private int limit;
    private Map<OperationType, Set<String>> requestedFields;
}

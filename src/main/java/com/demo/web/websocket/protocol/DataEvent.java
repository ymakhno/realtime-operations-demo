package com.demo.web.websocket.protocol;

import com.demo.to.QueueState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"newRows", "queueState"})
public class DataEvent extends Event {

    private String streamId;
    private List<Map<String, Object>> newRows;
    private QueueState queueState;
}

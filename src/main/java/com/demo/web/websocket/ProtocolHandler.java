package com.demo.web.websocket;

import com.demo.services.OperationsProcessor;
import com.demo.web.websocket.protocol.Command;
import com.demo.web.websocket.protocol.ErrorEvent;
import com.demo.web.websocket.protocol.Event;
import com.demo.web.websocket.protocol.StartStreamCommand;
import com.demo.web.websocket.protocol.StopStreamCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ProtocolHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final OperationsProcessor operationsProcessor;
    private final ObjectProvider<StreamHandler> streamHandlerProvider;
    private final Map<String, Map<String, StreamHandler>> streamHandlers = new ConcurrentHashMap<>();

    @Autowired
    public ProtocolHandler(ObjectMapper objectMapper, OperationsProcessor operationsProcessor,
                           ObjectProvider<StreamHandler> streamHandlerProvider) {
        this.objectMapper = objectMapper;
        this.operationsProcessor = operationsProcessor;
        this.streamHandlerProvider = streamHandlerProvider;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        parseCommand(session, message)
            .ifPresent(c -> processCommand(session, c));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.PROTOCOL_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Map<String, StreamHandler> handlers = streamHandlers.remove(session.getId());
        if (handlers != null) {
            handlers.values().forEach(StreamHandler::stopStream);
        }
    }

    private void processCommand(WebSocketSession session, Command command) {
        if (command instanceof StartStreamCommand) {
            StreamHandler handler = streamHandlerProvider.getObject(command, operationsProcessor);
            StreamHandler oldHandler = streamHandlers
                .computeIfAbsent(session.getId(), (id) -> new ConcurrentHashMap<>())
                .put(handler.getStreamId(), handler);
            if (oldHandler != null) {
                oldHandler.stopStream();
            }
            handler.startStream(e -> sendEvent(session, e));
        }
        if (command instanceof StopStreamCommand) {
            Optional.ofNullable(streamHandlers.get(session.getId()))
                .flatMap(m -> Optional.ofNullable(m.get(command.getStreamId())))
                .ifPresent(StreamHandler::stopStream);
        }
    }

    private Optional<Command> parseCommand(WebSocketSession session, TextMessage message) {
        try {
            return Optional.of(
                objectMapper.readValue(message.getPayload(), Command.class)
            );
        } catch (IOException e) {
            log.warn("Can't parse command", e);
            sendEvent(session, new ErrorEvent("", e.getMessage()));
            return Optional.empty();
        }
    }

    private void sendEvent(WebSocketSession session, Event event) {
        synchronized (session) {
            try {
                String message = objectMapper.writeValueAsString(event);
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Can't send message : " + event +
                    " to websocket", e);
            }
        }
    }
}

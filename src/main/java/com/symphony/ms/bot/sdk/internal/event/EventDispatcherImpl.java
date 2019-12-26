package com.symphony.ms.bot.sdk.internal.event;

import com.symphony.ms.bot.sdk.internal.event.model.BaseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventDispatcherImpl implements EventDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherImpl.class);

  private Map<String, List<BaseEventHandler>> eventHandlers = new HashMap<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public <E extends BaseEvent> void register(String channel, BaseEventHandler<E> handler) {
    LOGGER.info("Registering handler for event: {}", channel);
    List<BaseEventHandler> handlers = eventHandlers.get(channel);
    if (handlers == null || handlers.isEmpty()) {
      handlers = new ArrayList<>();
      handlers.add(handler);
      eventHandlers.put(channel, handlers);
    } else {
      handlers.add(handler);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Async("botTaskExecutor")
  public <E extends BaseEvent> void push(String channel, E event) {
    LOGGER.debug("Looking for handler for event: {}", channel);
    List<BaseEventHandler> handlers = eventHandlers.get(channel);
    if (handlers != null && !handlers.isEmpty()) {
      for (BaseEventHandler<E> handler : handlers) {
        LOGGER.debug("Handler found");
        handler.onEvent(event);
      }
    }
  }

}

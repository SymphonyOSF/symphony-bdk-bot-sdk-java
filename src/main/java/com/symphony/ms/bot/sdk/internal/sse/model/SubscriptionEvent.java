package com.symphony.ms.bot.sdk.internal.sse.model;

import com.symphony.ms.bot.sdk.internal.sse.SseSubscriber;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * SSE subscription event
 */
public class SubscriptionEvent {

  @Getter private List<String> eventTypes;
  @Getter private Map<String, String> metadata;
  @Getter private String lastEventId;
  @Getter private Long userId;
  private int subscriberHash;

  public SubscriptionEvent(SseSubscriber subscriber) {
    this.eventTypes = subscriber.getEventTypes();
    this.metadata = subscriber.getMetadata();
    this.lastEventId = subscriber.getLastEventId();
    this.userId = subscriber.getUserId();
    this.subscriberHash = subscriber.hashCode();
  }

  @Override
  public int hashCode() {
    return subscriberHash;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof SubscriptionEvent) {
      return object.hashCode() == this.hashCode();
    }
    return false;
  }

}

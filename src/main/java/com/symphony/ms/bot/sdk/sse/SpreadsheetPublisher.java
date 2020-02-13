package com.symphony.ms.bot.sdk.sse;

import com.symphony.ms.bot.sdk.internal.feature.FeatureManager;
import com.symphony.ms.bot.sdk.internal.sse.SsePublisher;
import com.symphony.ms.bot.sdk.internal.sse.SseSubscriber;
import com.symphony.ms.bot.sdk.internal.sse.model.SseEvent;
import com.symphony.ms.bot.sdk.internal.sse.model.SubscriptionEvent;
import com.symphony.ms.bot.sdk.internal.symphony.UsersClient;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;
import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyUser;
import com.symphony.ms.bot.sdk.spreadsheet.service.SpreadsheetPresenceEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sample code. Simple SsePublisher which waits for spreadsheet update events to send to the
 * clients.
 *
 * @author Gabriel Berberian
 */
public class SpreadsheetPublisher extends SsePublisher {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetPublisher.class);
  private static final String SPREADSHEET_PRESENCE_EVENT = "spreadsheetPresenceEvent";
  private static final long WAIT_INTERVAL = 1000L;
  private static final long PRESENCE_TIME_INTERVAL = 10000L;

  private final UsersClient usersClient;
  private final FeatureManager featureManager;
  private final AtomicLong eventId;
  private final Map<String, SpreadsheetPresenceEmitter> presenceEmitters;

  public SpreadsheetPublisher(UsersClient usersClient, FeatureManager featureManager) {
    this.usersClient = usersClient;
    this.featureManager = featureManager;
    this.eventId = new AtomicLong(0);
    this.presenceEmitters = new HashMap<>();
  }

  @Override
  public List<String> getEventTypes() {
    return Stream.of("spreadsheetUpdateEvent", "spreadsheetPresenceEvent")
        .collect(Collectors.toList());
  }

  @Override
  public void handleEvent(SseSubscriber subscriber, SseEvent event) {
    String subscriberStreamId = subscriber.getMetadata().get("streamId");
    String eventStreamId = event.getMetadata().get("streamId");
    if (subscriberStreamId == null || eventStreamId == null || subscriberStreamId.equals(
        eventStreamId)) {
      LOGGER.debug("Sending updates to user {}", subscriber.getUserId());
      subscriber.sendEvent(event);
    }
  }

  @Override
  protected void onSubscriberAdded(SubscriptionEvent subscriberAddedEvent) {
    SpreadsheetPresenceEmitter presenceEmitter =
        buildPresenceEmitter(subscriberAddedEvent, eventId);
    presenceEmitters.put(subscriberAddedEvent.getMetadata().get("subscriberUuid"), presenceEmitter);
    presenceEmitter.start();
  }

  @Override
  protected void onSubscriberRemoved(SubscriptionEvent subscriberRemovedEvent) {
    String subscriberUuid = subscriberRemovedEvent.getMetadata().get("subscriberUuid");
    presenceEmitters.get(subscriberUuid).finish();
    presenceEmitters.remove(subscriberUuid);
  }

  public Long getIdAndIncrement() {
    return eventId.getAndIncrement();
  }

  public SpreadsheetPresenceEmitter buildPresenceEmitter(
      SubscriptionEvent subscriberAddedEvent, AtomicLong eventId) {
    SymphonyUser user = getUserById(subscriberAddedEvent.getUserId());
    SseEvent presenceEvent =
        buildPresenceEvent(subscriberAddedEvent.getMetadata().get("streamId"), user);
    return new SpreadsheetPresenceEmitter(PRESENCE_TIME_INTERVAL, presenceEvent, eventId, this);
  }

  private SseEvent buildPresenceEvent(String streamId, SymphonyUser user) {
    return SseEvent.builder()
        .retry(WAIT_INTERVAL)
        .event(SPREADSHEET_PRESENCE_EVENT)
        .data(new HashMap<String, Object>() {
          {
            put("streamId", streamId);
            put("user", user);
          }
        })
        .metadata(new HashMap<String, String>() {
          {
            put("streamId", streamId);
          }
        }).build();
  }

  private SymphonyUser getUserById(long userId) {
    try {
      SymphonyUser user = usersClient.getUserFromId(userId, true);
      return user != null ? user : usersClient.getUserFromId(userId, false);
    } catch (SymphonyClientException e) {
      LOGGER.error("Exception getting user by id {}", userId);
      return null;
    }
  }

}

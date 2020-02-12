package com.symphony.ms.bot.sdk.spreadsheet.service;

import com.symphony.ms.bot.sdk.internal.sse.SsePublisher;
import com.symphony.ms.bot.sdk.internal.sse.model.SseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Emits presence events in a time interval
 */
public class SpreadsheetPresenceEmitter {
  private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetPresenceEmitter.class);

  private boolean finished;
  private Long timeInterval;
  private SseEvent presenceEvent;
  private AtomicLong eventId;
  private SsePublisher publisher;

  /**
   * Initializes all necessary resources for sending presence events
   *
   * @param timeInterval  the time interval between sending
   * @param presenceEvent the presence event model
   * @param eventId       the event id
   * @param publisher     the {@link SsePublisher} used for sending the presence events
   */
  public SpreadsheetPresenceEmitter(
      Long timeInterval, SseEvent presenceEvent, AtomicLong eventId, SsePublisher publisher) {
    if (timeInterval == null || presenceEvent == null) {
      throw new IllegalArgumentException(timeInterval == null ? "timeInterval" : "presenceEvent");
    }
    this.timeInterval = timeInterval;
    this.presenceEvent = presenceEvent;
    this.eventId = eventId;
    this.publisher = publisher;
  }

  /**
   * Starts sending presence events
   *
   * @return the {@link SpreadsheetPresenceEmitter} instance
   */
  public SpreadsheetPresenceEmitter start() {
    finished = false;
    new Thread(() -> {
      while (!finished) {
        this.presenceEvent.setId(Long.toString(eventId.getAndIncrement()));
        publisher.publishEvent(presenceEvent);
        waitInterval();
      }
    }).start();
    return this;
  }

  private void waitInterval() {
    try {
      Thread.sleep(timeInterval);
    } catch (InterruptedException ie) {
      LOGGER.debug("Error waiting time interval");
    }
  }

  /**
   * Stops sending presence events
   */
  public void finish() {
    this.finished = true;
  }
}

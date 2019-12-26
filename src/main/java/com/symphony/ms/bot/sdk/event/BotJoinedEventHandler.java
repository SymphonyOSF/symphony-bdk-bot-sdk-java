package com.symphony.ms.bot.sdk.event;

import com.symphony.ms.bot.sdk.internal.event.EventHandler;
import com.symphony.ms.bot.sdk.internal.event.model.UserJoinedRoomEvent;
import com.symphony.ms.bot.sdk.internal.message.model.SymphonyMessage;

import configuration.SymConfig;

/**
 * Sample code. Implementation of {@link EventHandler} to check if the user joining the room is the
 * configured bot and react to that.
 */
public class BotJoinedEventHandler extends EventHandler<UserJoinedRoomEvent> {

  private final String botUsername;

  public BotJoinedEventHandler(SymConfig symConfig) {
    this.botUsername = symConfig.getBotUsername();
  }

  @Override
  public void handle(UserJoinedRoomEvent event, SymphonyMessage eventResponse) {
    if (event.getUser().getUsername().equals(botUsername)) {
      eventResponse.setMessage("<mention uid=\"" + event.getUserId() +
          "\"/> was added. Symphony Bot Application features are now available in this room.");
    }
  }
}

package com.symphony.ms.bot.sdk.internal.command;

import com.symphony.ms.bot.sdk.internal.command.model.BotCommand;
import com.symphony.ms.bot.sdk.internal.feature.FeatureManager;
import com.symphony.ms.bot.sdk.internal.symphony.MessageClientImpl;
import com.symphony.ms.bot.sdk.internal.symphony.UsersClient;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;
import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyMessage;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Base class for bot command handling. Has it child classes automatically registered to {@link
 * CommandDispatcher} and {@link CommandFilter}. Provides mechanism for developers to define
 * responses for many rooms
 *
 * @author Gabriel Berberian
 */
@Setter
public abstract class MultiResponseCommandHandler implements BaseCommandHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

  protected CommandDispatcher commandDispatcher;

  protected CommandFilter commandFilter;

  protected FeatureManager featureManager;

  private MessageClientImpl messageClient;

  protected UsersClient usersClient;

  /**
   * Registers the CommandHandler to {@link CommandDispatcher} and {@link CommandFilter} so that it
   * can listen to and handle commands.
   */
  public void register() {
    init();
    commandDispatcher.register(getCommandName(), this);
    commandFilter.addFilter(getCommandName(), getCommandMatcher());
  }

  /**
   * Initializes the CommandHandler dependencies. This method can be overridden by the child classes
   * if the developers want to implement initialization logic.
   */
  protected void init() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onCommand(BotCommand command) {
    LOGGER.debug("Received command {}", command.getMessageEvent());
    final MultiResponseComposer multiResponseComposer = new MultiResponseComposerImpl();
    try {
      handle(command, multiResponseComposer);
      if (multiResponseComposer.hasContent() && featureManager.isCommandFeedbackEnabled()) {
        sendContent(multiResponseComposer);
      }
    } catch (Exception e) {
      LOGGER.error("Error processing command {}\n{}", getCommandName(), e);
      if (featureManager.unexpectedErrorResponse() != null) {
        messageClient._sendMessage(command.getMessageEvent().getStreamId(),
            new SymphonyMessage(featureManager.unexpectedErrorResponse()));
      }
    }
  }

  private void sendContent(MultiResponseComposer multiResponseComposer)
      throws MultiResponseComposer.UncompletedCommandResponseComposer {
    for (Map.Entry<SymphonyMessage, Set<String>> entry :
        multiResponseComposer.getComposedCommandResponse()
            .entrySet()) {
      SymphonyMessage symphonyMessage = entry.getKey();
      Set<String> streamIds = entry.getValue();
      sendMessageToStreams(symphonyMessage, streamIds);
    }
  }

  private void sendMessageToStreams(SymphonyMessage symphonyMessage, Set<String> streamIds) {
    for (String streamId : streamIds) {
      messageClient._sendMessage(streamId, symphonyMessage);
    }
  }

  /**
   * Returns the pattern used by {@link CommandFilter} to filter out bot commands.
   *
   * @return the matcher object
   */
  protected abstract Predicate<String> getCommandMatcher();

  /**
   * Handles a command issued to the bot
   *
   * @param command
   * @param multiResponseComposer
   */
  public abstract void handle(BotCommand command, final MultiResponseComposer multiResponseComposer)
      throws SymphonyClientException, MultiResponseComposer.UncompletedCommandResponseComposer;

  protected String getCommandName() {
    return this.getClass().getCanonicalName();
  }

  protected String getBotName() {
    return usersClient.getBotDisplayName();
  }

}

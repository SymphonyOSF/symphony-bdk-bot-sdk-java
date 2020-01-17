package com.symphony.ms.bot.sdk.command;

import com.symphony.ms.bot.sdk.internal.command.CommandHandler;
import com.symphony.ms.bot.sdk.internal.command.model.BotCommand;
import com.symphony.ms.bot.sdk.internal.event.model.MessageAttachmentFile;
import com.symphony.ms.bot.sdk.internal.message.model.SymphonyMessage;
import com.symphony.ms.bot.sdk.internal.symphony.MessageClient;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Sample code for CommandHandler that highlights the attachments of a Symphony message
 */
public class FileCommandHandler extends CommandHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileCommandHandler.class);

  private final MessageClient messageClient;

  public FileCommandHandler(MessageClient messageClient) {
    this.messageClient = messageClient;
  }

  @Override
  protected Predicate<String> getCommandMatcher() {
    return Pattern
        .compile("^@" + getBotName() + " /attachments?$")
        .asPredicate();
  }

  @Override
  public void handle(BotCommand command, SymphonyMessage commandResponse) {
    try {
      List<MessageAttachmentFile> attachments =
          messageClient.getMessageAttachments(command.getMessage().getMessageId(),
              command.getMessage().getStreamId(), command.getMessage().getAttachments());
      if (attachments == null || attachments.size() <= 0) {
        commandResponse.setMessage(
            "<mention uid=\"" + command.getMessage().getUserId()
                + "\"/> message has no attachment");
      } else if (attachments.size() == 1) {
        commandResponse.setMessage(
            "<mention uid=\"" + command.getMessage().getUserId()
                + "\"/> message has 1 attachment:");
        commandResponse.setAttachments(attachments);
      } else {
        commandResponse.setMessage(
            "<mention uid=\"" + command.getMessage().getUserId() + "\"/> message has "
                + attachments.size() + " attachments:");
        commandResponse.setAttachments(attachments);
      }
    } catch (SymphonyClientException sce) {
      LOGGER.error("SymphonyClientException thrown on FileCommandHandler", sce);
    }
  }

}

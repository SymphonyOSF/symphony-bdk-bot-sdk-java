package com.symphony.ms.bot.sdk.internal.symphony;

import com.symphony.ms.bot.sdk.internal.event.model.MessageAttachment;
import com.symphony.ms.bot.sdk.internal.event.model.MessageAttachmentFile;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;

import java.io.File;
import java.util.List;

/**
 * Message client
 *
 * @author msecato
 */
public interface MessageClient {

  /**
   * Sends message to a Symphony stream
   *
   * @param streamId
   * @param message
   * @param jsonData
   * @param attachments
   * @throws SymphonyClientException on error connecting to Symphony
   */
  void sendMessage(String streamId, String message, String jsonData, File[] attachments)
      throws SymphonyClientException;

  /**
   * Gets specific attachments from a message from a stream
   *
   * @param messageId
   * @param streamId
   * @param messageAttachments
   * @return the attachments
   */
  List<MessageAttachmentFile> getMessageAttachments(String messageId, String streamId,
      List<MessageAttachment> messageAttachments) throws SymphonyClientException;
}

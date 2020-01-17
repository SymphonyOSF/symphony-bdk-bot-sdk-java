package com.symphony.ms.bot.sdk.internal.message;

import com.symphony.ms.bot.sdk.internal.event.model.MessageAttachmentFile;
import com.symphony.ms.bot.sdk.internal.feature.FeatureManager;
import com.symphony.ms.bot.sdk.internal.lib.jsonmapper.JsonMapper;
import com.symphony.ms.bot.sdk.internal.lib.templating.TemplateService;
import com.symphony.ms.bot.sdk.internal.message.model.SymphonyMessage;
import com.symphony.ms.bot.sdk.internal.symphony.MessageClient;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);
  private static final String ENTITY_TAG = "<div class='entity' data-entity-id='%s'>%s</div>";

  private final MessageClient messageClient;
  private final TemplateService templateService;
  private final JsonMapper jsonMapper;
  private final FeatureManager featureManager;

  public MessageServiceImpl(MessageClient messageClient, TemplateService templateService,
      JsonMapper jsonMapper, FeatureManager featureManager) {
    this.messageClient = messageClient;
    this.templateService = templateService;
    this.jsonMapper = jsonMapper;
    this.featureManager = featureManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendMessage(String streamId, SymphonyMessage message) {
    String symMessage = getSymphonyMessage(message);
    String symJsonData = null;
    if (message.isEnrichedMessage()) {
      symMessage = entitify(message.getEntityName(), symMessage);
      symJsonData = getEnricherData(message);
    }
    try {
      UUID uuid = UUID.randomUUID();
      messageClient.sendMessage(streamId, symMessage, symJsonData,
          storeAttachments(message.getAttachments(), uuid));
      deleteAttachments(uuid);
    } catch (SymphonyClientException sce) {
      LOGGER.error("Could not send message to Symphony", sce);
    }
  }

  private String getSymphonyMessage(SymphonyMessage message) {
    String symMessage = message.getMessage();
    if (message.hasTemplate()) {
      symMessage = processTemplateMessage(message);
    }

    return symMessage;
  }

  private String getEnricherData(SymphonyMessage message) {
    return jsonMapper.toEnricherString(message.getEntityName(),
        message.getEntity(), message.getVersion());
  }

  private String processTemplateMessage(SymphonyMessage message) {
    String renderedString = null;
    if (message.usesTemplateFile()) {
      renderedString = templateService.processTemplateFile(
          message.getTemplateFile(), message.getTemplateData());
    } else {
      renderedString = templateService.processTemplateString(
          message.getTemplateString(), message.getTemplateData());
    }

    return renderedString;
  }

  private String entitify(String entityName, String content) {
    return String.format(ENTITY_TAG, entityName, content);
  }

  private File[] storeAttachments(List<MessageAttachmentFile> attachments, UUID uuid) {
    return attachments == null ? null : attachments.stream()
        .map(attachment -> storeAttachment(attachment, uuid))
        .toArray(File[]::new);
  }

  private File storeAttachment(MessageAttachmentFile attachment, UUID uuid) {
    try {
      String path = featureManager.getStorePath() + "/" + uuid + "/" + attachment.getFileName();
      File file = new File(path);
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }
      new FileOutputStream(file, false).write(attachment.getFileContent());
      return file;
    } catch (IOException e) {
      LOGGER.error("Failure storing attachment file", e);
    }
    return null;
  }

  private void deleteAttachments(UUID uuid) {
    String path = featureManager.getStorePath() + "/" + uuid;
    File directory = new File(path);
    if (directory.exists()) {
      File[] allContents = directory.listFiles();
      if (allContents != null) {
        for (File file : allContents) {
          if (!file.delete()) {
            LOGGER.error("Failure deleting attachment file {}", file.getPath());
          }
        }
      }
      if (!directory.delete()) {
        LOGGER.error("Failure deleting attachment file directory {}", directory.getPath());
      }
    }
  }

}

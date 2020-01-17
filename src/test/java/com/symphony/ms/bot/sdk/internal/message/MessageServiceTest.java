package com.symphony.ms.bot.sdk.internal.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.symphony.ms.bot.sdk.internal.event.model.MessageAttachmentFile;
import com.symphony.ms.bot.sdk.internal.feature.FeatureManager;
import com.symphony.ms.bot.sdk.internal.lib.jsonmapper.JsonMapper;
import com.symphony.ms.bot.sdk.internal.lib.templating.TemplateService;
import com.symphony.ms.bot.sdk.internal.message.model.SymphonyMessage;
import com.symphony.ms.bot.sdk.internal.symphony.MessageClient;
import com.symphony.ms.bot.sdk.internal.symphony.exception.SymphonyClientException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @Mock
  private MessageClient messageClient;

  @Mock
  private TemplateService templateService;

  @Mock
  private JsonMapper jsonMapper;

  @Mock
  private FeatureManager featureManager;

  @InjectMocks
  private MessageServiceImpl messageService;

  @Test
  public void sendMessageErrorTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.getMessage()).thenReturn("some message");
    when(message.hasTemplate()).thenReturn(false);
    when(message.isEnrichedMessage()).thenReturn(false);
    doThrow(new SymphonyClientException(new Exception()))
        .when(messageClient).sendMessage(any(), any(), any(), any());

    messageService.sendMessage("1234", message);
  }

  @Test
  public void sendSimpleMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.getMessage()).thenReturn("some message");
    when(message.hasTemplate()).thenReturn(false);
    when(message.isEnrichedMessage()).thenReturn(false);

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"), eq("some message"), eq(null), any());
  }

  @Test
  public void sendSimpleMessageWithEnrichedMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.getMessage()).thenReturn("some message");
    when(message.hasTemplate()).thenReturn(false);
    when(message.isEnrichedMessage()).thenReturn(true);
    when(message.getEntityName()).thenReturn("entity.name");
    doReturn("payload data")
        .when(jsonMapper).toEnricherString(anyString(), any(), any());

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"),
        eq("<div class='entity' data-entity-id='entity.name'>some message</div>"),
        eq("payload data"),
        any());
  }

  @Test
  public void sendTemplateStringMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.hasTemplate()).thenReturn(true);
    when(message.usesTemplateFile()).thenReturn(false);
    doReturn("some template message")
        .when(templateService).processTemplateString(any(), any());
    when(message.isEnrichedMessage()).thenReturn(false);

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"), eq("some template message"), eq(null), any());
  }

  @Test
  public void sendTemplateStringMessageWithEnrichedMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.hasTemplate()).thenReturn(true);
    when(message.usesTemplateFile()).thenReturn(false);
    doReturn("some template message")
        .when(templateService).processTemplateString(any(), any());
    when(message.isEnrichedMessage()).thenReturn(true);
    when(message.getEntityName()).thenReturn("entity.name");
    doReturn("payload data")
        .when(jsonMapper).toEnricherString(anyString(), any(), any());

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"),
        eq("<div class='entity' data-entity-id='entity.name'>some template message</div>"),
        eq("payload data"),
        any());
  }

  @Test
  public void sendTemplateFileMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.hasTemplate()).thenReturn(true);
    when(message.usesTemplateFile()).thenReturn(true);
    doReturn("some template file")
        .when(templateService).processTemplateFile(any(), any());
    when(message.isEnrichedMessage()).thenReturn(false);

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"), eq("some template file"), eq(null), any());
  }

  @Test
  public void sendTemplateFileMessageWithEnrichedMessageTest() throws Exception {
    SymphonyMessage message = mock(SymphonyMessage.class);
    when(message.hasTemplate()).thenReturn(true);
    when(message.usesTemplateFile()).thenReturn(true);
    doReturn("some template file")
        .when(templateService).processTemplateFile(any(), any());
    when(message.isEnrichedMessage()).thenReturn(true);
    when(message.getEntityName()).thenReturn("entity.name");
    doReturn("payload data")
        .when(jsonMapper).toEnricherString(anyString(), any(), any());

    messageService.sendMessage("1234", message);

    verify(messageClient, times(1)).sendMessage(
        eq("1234"),
        eq("<div class='entity' data-entity-id='entity.name'>some template file</div>"),
        eq("payload data"),
        any());
  }

  @Test
  public void shouldStoreAndDeleteAttachments() throws SymphonyClientException {
    when(featureManager.getStorePath()).thenReturn("/tmp/symphony/test");
    SymphonyMessage message = new SymphonyMessage();
    MessageAttachmentFile attachmentFile = mock(MessageAttachmentFile.class);
    when(attachmentFile.getFileContent()).thenReturn("content".getBytes());
    when(attachmentFile.getFileName()).thenReturn(getAttachmentName());
    message.setAttachments(Collections.singletonList(attachmentFile));
    message.setMessage("message");

    messageService.sendMessage("streamId", message);

    ArgumentCaptor<File[]> argumentCaptor = ArgumentCaptor.forClass(File[].class);
    verify(messageClient, times(1)).sendMessage(
        anyString(), anyString(), any(), argumentCaptor.capture());
    File[] value = argumentCaptor.getValue();
    assertEquals(1, value.length);
    assertTrue(new File("/tmp/symphony/test").exists());
    tearDown();
  }

  private String getAttachmentName() {
    return "attachment_test " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .format(LocalDateTime.now()) + ".txt";
  }

  public void tearDown() {
    File testDir = new File("/tmp/symphony/test");
    if (testDir.exists()) {
      for (File file : testDir.listFiles()) {
        file.delete();
      }
      testDir.delete();
    }
  }

}

package com.symphony.ms.bot.sdk.internal.command;

import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyMessage;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link MultiResponseComposer}
 *
 * @author Gabriel Berberian
 */
@NoArgsConstructor
public class MultiResponseComposerImpl extends MultiResponseComposer {

  private SymphonyMessage message;
  private MultiResponseStreamsComposer streamsComposer;
  private Map<SymphonyMessage, Set<String>> composedCommandResponse;

  protected MultiResponseComposerImpl(MultiResponseStreamsComposer streamsComposer) {
    this.streamsComposer = streamsComposer;
  }

  @Override
  public MultiResponseStreamsComposer message(String message) {
    this.message = new SymphonyMessage(message);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public MultiResponseStreamsComposer enrichedMessage(String message, String entityName,
      Object entity, String version) {
    this.message = new SymphonyMessage();
    this.message.setEnrichedMessage(message, entityName, entity, version);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public MultiResponseStreamsComposer templateMessage(String templateMessage,
      Object templateData) {
    this.message = new SymphonyMessage();
    this.message.setTemplateMessage(templateMessage, templateData);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public MultiResponseStreamsComposer enrichedTemplateMessage(String templateMessage,
      Object templateData, String entityName, Object entity, String version) {
    this.message = new SymphonyMessage();
    this.message.setEnrichedTemplateMessage(
        templateMessage, templateData, entityName, entity, version);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public MultiResponseStreamsComposer templateFile(String templateFile, Object templateData) {
    this.message = new SymphonyMessage();
    this.message.setTemplateFile(templateFile, templateData);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public MultiResponseStreamsComposer enrichedTemplateFile(String templateFile,
      Object templateData, String entityName, Object entity, String version) {
    this.message = new SymphonyMessage();
    this.message.setEnrichedTemplateFile(templateFile, templateData, entityName, entity, version);
    return new MultiResponseStreamComposerImpl(this);
  }

  @Override
  public void compose() throws UncompletedCommandResponseComposer {
    if (streamsComposer == null) {
      throw new UncompletedCommandResponseComposer("Cannot compose");
    }
    composedCommandResponse = streamsComposer.complete();
  }

  @Override
  protected boolean hasContent() {
    return composedCommandResponse != null;
  }

  @Override
  protected Map<SymphonyMessage, Set<String>> complete() {
    if (streamsComposer == null) {
      this.composedCommandResponse = new HashMap<>();
      return composedCommandResponse;
    } else {
      return streamsComposer.complete();
    }
  }

  @Override
  protected Map<SymphonyMessage, Set<String>> getComposedCommandResponse()
      throws UncompletedCommandResponseComposer {
    if (composedCommandResponse == null) {
      throw new UncompletedCommandResponseComposer("Cannot get command response");
    }
    return composedCommandResponse;
  }

  @Override
  protected SymphonyMessage getMessage() {
    return this.message;
  }

}

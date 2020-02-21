package com.symphony.ms.bot.sdk.internal.command;

import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyMessage;

import java.util.Map;
import java.util.Set;

/**
 * Abstraction of composer to be used by developers to bind messages to recipient streams
 *
 * @author Gabriel Berberian
 */
public abstract class MultiResponseComposer {

  public abstract MultiResponseStreamsComposer message(String message);

  public abstract MultiResponseStreamsComposer enrichedMessage(
      String message, String entityName, Object entity, String version);

  public abstract MultiResponseStreamsComposer templateMessage(String templateMessage,
      Object templateData);

  public abstract MultiResponseStreamsComposer enrichedTemplateMessage(String templateMessage,
      Object templateData, String entityName, Object entity, String version);

  public abstract MultiResponseStreamsComposer templateFile(String templateFile,
      Object templateData);

  public abstract MultiResponseStreamsComposer enrichedTemplateFile(String templateFile,
      Object templateData,
      String entityName, Object entity, String version);

  public abstract void compose() throws UncompletedCommandResponseComposer;

  protected abstract boolean hasContent();

  protected abstract Map<SymphonyMessage, Set<String>> complete();

  protected abstract Map<SymphonyMessage, Set<String>> getComposedCommandResponse()
      throws UncompletedCommandResponseComposer;

  protected abstract SymphonyMessage getMessage();

  public class UncompletedCommandResponseComposer extends Exception {

    public UncompletedCommandResponseComposer(String message) {
      super(message + ". Command response composer uncompleted");
    }
  }

}

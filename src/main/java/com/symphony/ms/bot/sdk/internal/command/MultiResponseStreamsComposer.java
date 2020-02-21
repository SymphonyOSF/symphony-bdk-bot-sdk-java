package com.symphony.ms.bot.sdk.internal.command;

import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyMessage;

import java.util.Map;
import java.util.Set;

/**
 * Abstraction of composer to be used by developers to bind messages to recipient streams
 *
 * @author Gabriel Berberian
 */
public abstract class MultiResponseStreamsComposer {

  public abstract MultiResponseComposer streamIds(Set<String> streamIds);

  protected abstract Map<SymphonyMessage, Set<String>> complete();
}

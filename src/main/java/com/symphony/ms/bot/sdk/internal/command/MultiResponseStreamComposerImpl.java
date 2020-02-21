package com.symphony.ms.bot.sdk.internal.command;

import com.symphony.ms.bot.sdk.internal.symphony.model.SymphonyMessage;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link MultiResponseStreamsComposer}
 *
 * @author Gabriel Berberian
 */
public class MultiResponseStreamComposerImpl extends MultiResponseStreamsComposer {

  private MultiResponseComposer responseComposer;
  private Set<String> streamIds;

  protected MultiResponseStreamComposerImpl(MultiResponseComposer responseComposer) {
    this.responseComposer = responseComposer;
  }

  @Override
  public MultiResponseComposer streamIds(Set<String> streamIds) {
    this.streamIds = streamIds;
    return new MultiResponseComposerImpl(this);
  }

  @Override
  protected Map<SymphonyMessage, Set<String>> complete() {
    Map<SymphonyMessage, Set<String>> map = responseComposer.complete();
    map.put(responseComposer.getMessage(), streamIds);
    return map;
  }

}

package com.symphony.ms.songwriter.command;

import static com.symphony.ms.songwriter.internal.lib.commandmatcher.CommandMatcherBuilder.simpleCommandMatcher;

import com.symphony.ms.songwriter.internal.command.CommandHandler;
import com.symphony.ms.songwriter.internal.command.model.BotCommand;
import com.symphony.ms.songwriter.internal.message.model.SymphonyMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Sample code. Simple hello message.
 */
public class HelloCommandHandler extends CommandHandler {

  @Override
  protected Predicate<String> getCommandMatcher() {
    return simpleCommandMatcher(getBotName(), "hello").predicate();
  }

  /**
   * Invoked when command matches
   */
  @Override
  public void handle(BotCommand command, SymphonyMessage response) {
    Map<String, String> variables = new HashMap<>();
    variables.put("user", command.getUserDisplayName());

    response.setTemplateMessage("Hello, <b>{{user}}</b>", variables);
  }

}

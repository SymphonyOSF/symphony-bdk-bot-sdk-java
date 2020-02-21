package com.symphony.ms.bot.sdk.internal.command.config;

import com.symphony.ms.bot.sdk.internal.command.MultiResponseCommandHandler;
import com.symphony.ms.bot.sdk.internal.scan.BaseBeanFactoryPostProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Automatically scans for {@link MultiResponseCommandHandler}, instantiates them, injects all
 * dependencies and registers to Spring bean registry.
 *
 * @author Gabriel Berberian
 */
@Configuration
public class MultiResponseBeanFactoryProcessor extends BaseBeanFactoryPostProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandBeanFactoryProcessor.class);

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    final BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
    Set<BeanDefinition> beanDefinitionSet = scanComponents(MultiResponseCommandHandler.class);
    LOGGER.info("Scanning for command handlers found {} beans", beanDefinitionSet.size());

    for (BeanDefinition beanDefinition : beanDefinitionSet) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder
          .genericBeanDefinition(beanDefinition.getBeanClassName())
          .setInitMethodName("register")
          .addPropertyReference("commandDispatcher", "commandDispatcherImpl")
          .addPropertyReference("commandFilter", "commandFilterImpl")
          .addPropertyReference("messageClient", "messageClientImpl")
          .addPropertyReference("featureManager", "featureManager")
          .addPropertyReference("usersClient", "usersClientImpl");

      beanDefinitionRegistry.registerBeanDefinition(
          beanDefinition.getBeanClassName(), builder.getBeanDefinition());
    }
  }

}

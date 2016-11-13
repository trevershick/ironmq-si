package com.github.trevershick.si.ironmq.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.github.trevershick.si.ironmq.IronMqDefaultClientFactory;

public class IronMqDefaultClientFactoryParser extends AbstractBeanDefinitionParser {

  @Override
  protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
    final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(IronMqDefaultClientFactory.class);
    builder.addConstructorArgValue(element.getAttribute(IronMqParserConstants.ATTRIBUTE_PROJECT_ID));
    builder.addConstructorArgValue(element.getAttribute(IronMqParserConstants.ATTRIBUTE_TOKEN));
    builder.addConstructorArgValue(element.getAttribute(IronMqParserConstants.ATTRIBUTE_CLOUD));
    return builder.getBeanDefinition();
  }
}

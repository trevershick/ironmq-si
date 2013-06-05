package com.github.trevershick.si.ironmq.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.github.trevershick.si.ironmq.DefaultIronMqClientFactory;

public class DefaultIronMqClientFactoryParser extends AbstractBeanDefinitionParser {

	
	public static final String PROJECT_ID = "ironmq-project";
	public static final String TOKEN = "ironmq-token";


	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( DefaultIronMqClientFactory.class );
		builder.addConstructorArgValue(element.getAttribute(PROJECT_ID));
		builder.addConstructorArgValue(element.getAttribute(TOKEN));
		return builder.getBeanDefinition();
	}
}

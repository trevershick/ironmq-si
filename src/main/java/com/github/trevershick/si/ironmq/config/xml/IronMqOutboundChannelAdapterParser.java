package com.github.trevershick.si.ironmq.config.xml;

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

import com.github.trevershick.si.ironmq.outbound.IronMqQueueingMessageHandler;

public class IronMqOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

	/**
	 * Creates the "int-ironmq:outbound-channel-adapter" bean definition.
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	protected AbstractBeanDefinition parseConsumer(Element element,
			ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(IronMqQueueingMessageHandler.class);
		
		
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, 
				IronMqParserConstants.ATTRIBUTE_CLIENT_FACTORY, 
				IronMqParserConstants.PROPERTY_CLIENT_FACTORY);
		
		final boolean hasQueueName = element.hasAttribute(IronMqParserConstants.ATTRIBUTE_QUEUE_NAME);
		final boolean hasQueueNameExpression = element.hasAttribute(IronMqParserConstants.ATTRIBUTE_QUEUE_NAME_EXPRESSION);
		if (hasQueueName && hasQueueNameExpression) {
			parserContext.getReaderContext().error(
					String.format("At most one of '%s' or '%s' is allowed", 
							IronMqParserConstants.ATTRIBUTE_QUEUE_NAME, 
							IronMqParserConstants.ATTRIBUTE_QUEUE_NAME_EXPRESSION)
					, element);
		}
		if (hasQueueName) {
			builder.addPropertyValue(IronMqParserConstants.PROPERTY_QUEUE_NAME,
					new TypedStringValue(element.getAttribute(IronMqParserConstants.ATTRIBUTE_QUEUE_NAME)));
		}
		if (hasQueueNameExpression) {
			RootBeanDefinition expressionDef = new RootBeanDefinition(ExpressionFactoryBean.class);
			expressionDef.getConstructorArgumentValues().addGenericArgumentValue(
					element.getAttribute(IronMqParserConstants.ATTRIBUTE_QUEUE_NAME_EXPRESSION));
			builder.addPropertyValue(IronMqParserConstants.PROPERTY_QUEUE_NAME_EXPRESSION, expressionDef);
		}
		

		return builder.getBeanDefinition();
	}

}

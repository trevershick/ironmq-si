package com.github.trevershick.si.ironmq.config.xml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

import com.github.trevershick.si.ironmq.inbound.IronMqMessageSource;

public class IronMqInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {


	@Override
	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( IronMqMessageSource.class );

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, 
				IronMqParserConstants.ATTRIBUTE_CLIENT_FACTORY, 
				IronMqParserConstants.PROPERTY_CLIENT_FACTORY);
		
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IronMqParserConstants.ATTRIBUTE_EXTRACT_PAYLOAD,
				IronMqParserConstants.PROPERTY_EXTRACT_PAYLOAD);
 		
		final RootBeanDefinition queueNameExpressionDefinition =
				IntegrationNamespaceUtils.createExpressionDefinitionFromValueOrExpression(
						IronMqParserConstants.ATTRIBUTE_QUEUE_NAME, 
						IronMqParserConstants.ATTRIBUTE_QUEUE_NAME_EXPRESSION,
						parserContext, element, true);
		
		builder.addPropertyValue(IronMqParserConstants.PROPERTY_QUEUE_NAME_EXPRESSION, queueNameExpressionDefinition );

		return builder.getBeanDefinition();
	}
}

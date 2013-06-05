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

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "client-factory","clientFactory");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "extract-payload","extractPayload");
 		
		final RootBeanDefinition queueNameExpressionDefinition =
				IntegrationNamespaceUtils.createExpressionDefinitionFromValueOrExpression(
						"queue-name", "queue-name-expression",
						parserContext, element, true);
		
		builder.addPropertyValue("queueNameExpression", queueNameExpressionDefinition );

		return builder.getBeanDefinition();
	}
}

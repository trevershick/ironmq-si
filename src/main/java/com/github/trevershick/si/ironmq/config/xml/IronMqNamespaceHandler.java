package com.github.trevershick.si.ironmq.config.xml;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

public class IronMqNamespaceHandler extends AbstractIntegrationNamespaceHandler {
	
	public void init() {
		registerBeanDefinitionParser( "inbound-channel-adapter", new IronMqInboundChannelAdapterParser() );
		registerBeanDefinitionParser( "default-client-factory", new DefaultIronMqClientFactoryParser() );
	}
}

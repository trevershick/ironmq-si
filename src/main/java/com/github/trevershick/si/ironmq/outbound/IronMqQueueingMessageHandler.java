package com.github.trevershick.si.ironmq.outbound;

import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;

import java.io.IOException;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.handler.AbstractMessageHandler;

import com.github.trevershick.si.ironmq.IronMqClientFactory;
import com.github.trevershick.si.ironmq.inbound.IronMqMessageHeaders;
import com.google.gson.Gson;

public class IronMqQueueingMessageHandler extends AbstractMessageHandler {
	private volatile IronMqClientFactory clientFactory;
	/**
	 * evaluated on every call to the {@link #receive()} method.
	 */
	private volatile Expression queueNameExpression = new SpelExpressionParser().parseExpression( "headers." + IronMqMessageHeaders.QUEUE);
	private volatile StandardEvaluationContext evaluationContext;


	
	
	public IronMqClientFactory getClientFactory() {
		return clientFactory;
	}


	public void setClientFactory(IronMqClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}


	@Override
	protected void onInit() throws Exception {
		super.onInit();
		if (getBeanFactory() != null) {
			evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
		} else {
			evaluationContext = ExpressionUtils.createStandardEvaluationContext();
		}
	}


	@Override
	public String getComponentType() {
		return "ironmq:outbound-channel-adapter";
	}

	public void setQueueName(String value) {
		setQueueNameExpression(new LiteralExpression(value));
	}
	
	public void setQueueNameExpression(Expression qnExpression) {
		this.queueNameExpression = qnExpression;
	}
	

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		final String queueName = queueNameExpression.getValue( evaluationContext, message, String.class );
		final Client client = clientFactory.getClient();
		if (client == null) {
			throw new NullPointerException("Unable to obtain client from factory " + clientFactory);
		}
		final Queue q = client.queue(queueName);
		if (q == null) {
			throw new NullPointerException("Unable to obtain queue " + queueName + " from client " + client);
		}
		if (message.getPayload() == null) {// is this even possible
			logger.debug("Skipping message sending because the payload is null");
			return;
		}
		try {
			
			MessageHeaders headers = message.getHeaders();
			long delay = longHeader(headers, IronMqMessageHeaders.DELAY);
			long expires = longHeader(headers, IronMqMessageHeaders.EXPIRES_IN);
			long timeout = longHeader(headers, IronMqMessageHeaders.TIMEOUT);
			String body = message.getPayload().toString();
			
			if (logger.isDebugEnabled()) {
				io.iron.ironmq.Message m = new io.iron.ironmq.Message();
				m.setBody(body);
				m.setDelay(delay);
				m.setExpiresIn(expires);
				m.setTimeout(timeout);
				Gson gson = new Gson();
		        logger.debug("send :" + gson.toJson(m));				
			}
			q.push(body,timeout,delay,expires);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	private long longHeader(MessageHeaders headers, String key) {
		if (headers == null) { return 0; }
		Object obj = headers.get(key);
		if (obj == null) { return 0; }
		try {
			return new DefaultConversionService().convert(obj,Long.class);
		} catch (Exception e) {
			return new DefaultConversionService().convert(String.valueOf(obj),Long.class);
		}
	}

}

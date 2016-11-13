package com.github.trevershick.si.ironmq.outbound;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.trevershick.si.ironmq.inbound.IronMqMessageHeaders;
import com.github.trevershick.si.ironmq.inbound.MockClientFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unchecked")
public class IronMQOutboundAdapterTest {
	
	@Autowired
	@Qualifier("c1")
	MessageChannel c1;
	
	@Autowired
	@Qualifier("c2")
	MessageChannel c2;
	
	@Autowired
	@Qualifier("c3")
	MessageChannel c3;
	
	@Autowired
	MockClientFactory mockClientFactory;

	@Before
	public void clearSharedMock() {
		reset(mockClientFactory.getClient());
	}
	
	@Test
	public void verifyHeadersPassed() throws IOException { 
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue = mock(Queue.class);
		when(mockClient.queue("queue3")).thenReturn(queue);
		
		// the message sender does it's best to convert any header value
		long timeout = 1000;
		Object delay = new Object() {
			public String toString() {
				return "250";
			}
		};
		String expiresIn = "125";
		
		
		// act
		c3.send(MessageBuilder
				.withPayload("message3")
				.setHeader("ironmq_queue", "queue3")
				.setHeader(IronMqMessageHeaders.DELAY, delay)
				.setHeader(IronMqMessageHeaders.EXPIRES_IN, expiresIn)
				.setHeader(IronMqMessageHeaders.TIMEOUT, timeout)
				.build());

		// assert
		verify(mockClient).queue("queue3");
		verify(queue).push("message3", 1000);
	}
	
	
	
	@Test(expected=MessageHandlingException.class)
	public void verifyHeadersAreJustWrong() throws IOException { 
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue = mock(Queue.class);
		when(mockClient.queue("queue3")).thenReturn(queue);
		
		// the message sender does it's best to convert any header value
		String timeout = "x";
		
		
		// act
		c3.send(MessageBuilder
				.withPayload("message3")
				.setHeader("ironmq_queue", "queue3")
				.setHeader(IronMqMessageHeaders.TIMEOUT, timeout)
				.build());

		// assert
		verify(mockClient).queue("queue3");
		verify(queue,times(0)).push(anyString(),anyLong());
	}
	
	@Test
	public void testSendToNamedQueue() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue1 = mock(Queue.class);
		when(mockClient.queue("queue1")).thenReturn(queue1);
		
		// act
		c1.send(MessageBuilder.withPayload("message1").build());

		// assert
		verify(mockClient).queue("queue1");
		verify(queue1).push("message1", 0);
	}
	
	@Test
	public void testSendToQueueByExpression() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue = mock(Queue.class);
		when(mockClient.queue("queue2")).thenReturn(queue);
		
		// act
		c2.send(MessageBuilder.withPayload("message2").build());

		// assert
		verify(mockClient).queue("queue2");
		verify(queue).push("message2", 0);
	}
	
	@Test
	public void testSendToQueueFromHeader() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue = mock(Queue.class);
		when(mockClient.queue("queue3")).thenReturn(queue);
		
		// act
		c3.send(MessageBuilder
				.withPayload("message3")
				.setHeader("ironmq_queue", "queue3")
				.build());

		// assert
		verify(mockClient).queue("queue3");
		verify(queue).push("message3", 0);
	}

}

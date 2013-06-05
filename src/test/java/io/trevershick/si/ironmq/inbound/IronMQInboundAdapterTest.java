package io.trevershick.si.ironmq.inbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;
import io.trevershick.si.ironmq.DefaultIronMqClientFactory;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unchecked")
public class IronMQInboundAdapterTest {
	@Autowired
	DefaultIronMqClientFactory dimcf;
	
	@Autowired
	@Qualifier("ironmqInboundChannel")
	PollableChannel inboundChannel;
	
	@Autowired
	MockClientFactory mockClientFactory;
	
	@Autowired
	@Qualifier("ironmqInTx")
	Object obj;
	
	
	@Test
	public void factory_was_created(){
		assertEquals("thepid", dimcf.getProjectId());
		assertEquals("tkn", dimcf.getToken());
	}
	
	/**
	 * Tests inbound adapter configured with "queue-name" attribute.
	 * @throws IOException 
	 */
	@Test
	public void testReceiveMessageKey() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue1 = mock(Queue.class);
		
		when(mockClient.queue("queue1")).thenReturn(queue1);
		
		io.iron.ironmq.Message message = new io.iron.ironmq.Message();
		message.setId("theid");
		message.setBody("xxx");
		when(queue1.get()).thenReturn(message).thenReturn(null);
		
		// act (done by poller)
		// this blocks until the message arrives.
		final Message<String> received = (Message<String>) inboundChannel.receive();

		verify(queue1).deleteMessage("theid");
		assertEquals("xxx", received.getPayload());
	}

	@Test
	public void testReceiveMessageQueueNameIsExpression() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue1 = mock(Queue.class);
		
		when(mockClient.queue("queue2")).thenReturn(queue1);
		
		io.iron.ironmq.Message message = new io.iron.ironmq.Message();
		message.setId("theid2");
		message.setBody("xxx2");
		when(queue1.get()).thenReturn(message).thenReturn(null);
		
		// act (done by poller)
		// this blocks until the message arrives.
		final Message<String> received = (Message<String>) inboundChannel.receive(3000);

		assertNotNull("Should have received a message on the channel", received);
		verify(queue1).deleteMessage("theid2");
		assertEquals("xxx2", received.getPayload());
	}
	
	@Test
	public void testReceiveUnExtractedPayload() throws IOException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue1 = mock(Queue.class);
		
		when(mockClient.queue("queue3")).thenReturn(queue1);
		
		io.iron.ironmq.Message message = new io.iron.ironmq.Message();
		message.setId("theid3");
		message.setBody("xxx3");
		when(queue1.get()).thenReturn(message).thenReturn(null);
		
		// act (done by poller)
		// this blocks until the message arrives.
		final Message<io.iron.ironmq.Message> received = (Message<io.iron.ironmq.Message>) inboundChannel.receive(3000);

		assertNotNull("Should have received a message on the channel", received);
		verify(queue1).deleteMessage("theid3");

		assertEquals("xxx3",received.getPayload().getBody());
		assertEquals("theid3", received.getPayload().getId());
	}
	
	@Test
	public void testReceived_with_tx() throws IOException, InterruptedException {
		// assemble
		Client mockClient = mockClientFactory.getClient();
		Queue queue1 = mock(Queue.class);
		
		when(mockClient.queue("queuetx")).thenReturn(queue1);
		
		io.iron.ironmq.Message message = new io.iron.ironmq.Message();
		message.setId("theidtx");
		message.setBody("xxxtx");
		when(queue1.get()).thenReturn(message).thenReturn(null);
		
		// act (done by poller)
		// this blocks until the message arrives.
		final Message<io.iron.ironmq.Message> received = (Message<io.iron.ironmq.Message>) inboundChannel.receive(3000);
		assertNotNull("Should have received a message on the channel", received);

		// sleep a little, the poller needs to finish it's transaction synchronization...
		// i'm sure there's a better way to do this, but this works for now.
		Thread.sleep(1000);
		verify(queue1).deleteMessage("theidtx");

		assertEquals("xxxtx",received.getPayload().getBody());
		assertEquals("theidtx", received.getPayload().getId());
	}

}

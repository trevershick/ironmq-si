package com.github.trevershick.si.ironmq.inbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.iron.ironmq.Client;
import io.iron.ironmq.EmptyQueueException;
import io.iron.ironmq.Queue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.trevershick.si.ironmq.IronMqDefaultClientFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unchecked")
public class IronMQInboundAdapterTest {
  @Autowired
  IronMqDefaultClientFactory dimcf;

  @Autowired
  @Qualifier("ironmqInboundChannel")
  PollableChannel inboundChannel;

  @Autowired
  @Qualifier("ironmqInQueueName")
  SourcePollingChannelAdapter adapter1;

  @Autowired
  @Qualifier("ironmqQueueExpr")
  SourcePollingChannelAdapter adapter2;

  @Autowired
  @Qualifier("ironmqInUnwrapped")
  SourcePollingChannelAdapter adapter3;

  @Autowired
  @Qualifier("ironmqInTx")
  SourcePollingChannelAdapter adapterTx;

  @Autowired
  MockClientFactory mockClientFactory;

  @Autowired
  @Qualifier("ironmqInTx")
  Object obj;

  @Test
  public void testTheFactoryWasCreatedWithAppropriateArguments() {
    assertEquals("Ensure the expression was evaluated", "thepid", dimcf.getProjectId());
    assertEquals("Token was passed in", "tkn", dimcf.getToken());
  }

  /**
   * Tests inbound adapter configured with "queue-name" attribute.
   *
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
    message.setDelay(0);
    message.setTimeout(0);
    message.setExpiresIn(1000);
    message.setReservationId("rid");

    when(queue1.reserve(Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(new io.iron.ironmq.Messages(message))
      .thenThrow(EmptyQueueException.class);

    // act (done by poller)
    // this blocks until the message arrives.
    this.adapter1.start();
    final Message<String> received = (Message<String>) inboundChannel.receive();
    this.adapter1.stop();

    verify(queue1).deleteMessage("theid", "rid");
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
    message.setDelay(0);
    message.setTimeout(0);
    message.setExpiresIn(1000);
    message.setReservationId("rid");

    when(queue1.reserve(Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(new io.iron.ironmq.Messages(message))
      .thenThrow(EmptyQueueException.class);

    // act (done by poller)
    // this blocks until the message arrives.
    this.adapter2.start();
    final Message<String> received = (Message<String>) inboundChannel.receive(3000);
    this.adapter2.stop();

    assertNotNull("Should have received a message on the channel", received);
    verify(queue1).deleteMessage("theid2", "rid");
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
    message.setDelay(0);
    message.setTimeout(0);
    message.setExpiresIn(1000);
    message.setReservationId("rid");
    when(queue1.reserve(Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(new io.iron.ironmq.Messages(message)).thenThrow(EmptyQueueException.class);

    // act (done by poller)
    // this blocks until the message arrives.
    this.adapter3.start();
    final Message<io.iron.ironmq.Message> received = (Message<io.iron.ironmq.Message>) inboundChannel.receive(3000);
    this.adapter3.stop();

    assertNotNull("Should have received a message on the channel", received);
    verify(queue1).deleteMessage("theid3", "rid");

    assertEquals("xxx3", received.getPayload().getBody());
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
    message.setDelay(0);
    message.setTimeout(0);
    message.setExpiresIn(1000);

    when(queue1.reserve(Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(new io.iron.ironmq.Messages(message))
      .thenThrow(EmptyQueueException.class);

    // act (done by poller)
    // this blocks until the message arrives.
    this.adapterTx.start();
    final Message<io.iron.ironmq.Message> received = (Message<io.iron.ironmq.Message>) inboundChannel.receive(3000);
    this.adapterTx.stop();

    assertNotNull("Should have received a message on the channel", received);
    // sleep a little, the poller needs to finish it's transaction synchronization...
    // i'm sure there's a better way to do this, but this works for now.
    Thread.sleep(1000);
    verify(queue1).deleteMessage("theidtx");

    assertEquals("xxxtx", received.getPayload().getBody());
    assertEquals("theidtx", received.getPayload().getId());
  }
}

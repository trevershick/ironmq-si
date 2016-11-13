package com.github.trevershick.si.ironmq;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;

import java.io.IOException;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by tshick on 11/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("IntegrationTest.xml")
public class IntegrationTest {
  @Autowired
  @Qualifier("outboundChannel")
  MessageChannel outbound;

  @Autowired
  @Qualifier("inboundChannel")
  PollableChannel inbound;

  @Autowired
  @Qualifier("ironMqClient")
  Client client;

  @Before
  @After
  public void clearQueue() throws IOException {
    final Queue q = client.queue("int-test");
    q.clear();
  }

  @Test
  public void putAndGet() throws IOException {
    Assume.assumeThat("IRONMQ_PROJECT should be set",
      System.getenv("IRONMQ_PROJECT"), is(not(nullValue())));
    Assume.assumeThat("IRONMQ_TOKEN should be set",
      System.getenv("IRONMQ_TOKEN"), is(not(nullValue())));
    Assume.assumeThat("IRONMQ_CLOUD_URL should be set",
      System.getenv("IRONMQ_CLOUD_URL"), is(not(nullValue())));

    final String contents = "message-" + System.currentTimeMillis();

    final Message<String> m = MessageBuilder
      .withPayload(contents)
      .build();
    outbound.send(m);

    final Message<?> received = inbound.receive();
    String in = (String) received.getPayload();

    assertThat(in, is(contents));
  }
}

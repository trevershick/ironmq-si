package com.github.trevershick.si.ironmq.inbound;

import io.iron.ironmq.Client;
import io.iron.ironmq.EmptyQueueException;
import io.iron.ironmq.Queue;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import com.github.trevershick.si.ironmq.IronMqClientFactory;

public class IronMqMessageSource extends IntegrationObjectSupport implements MessageSource<Serializable> {

  private volatile int reservationInSeconds = 60;
  private volatile boolean extractPayload = true;

  private volatile IronMqClientFactory clientFactory;
  /**
   * evaluated on every call to the {@link #receive()} method.
   */
  private volatile Expression queueNameExpression;

  private volatile StandardEvaluationContext evaluationContext;

  public IronMqClientFactory getClientFactory() {
    return clientFactory;
  }

  public void setClientFactory(IronMqClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  public boolean isExtractPayload() {
    return extractPayload;
  }

  public void setExtractPayload(boolean extractPayload) {
    this.extractPayload = extractPayload;
  }

  public int getReservationInSeconds() {
    return reservationInSeconds;
  }

  public void setReservationInSeconds(int reservationInSeconds) {
    this.reservationInSeconds = reservationInSeconds;
  }

  @Override
  protected void onInit() throws Exception {
    super.onInit();
    if (getBeanFactory() != null) {
      evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
    }
    else {
      evaluationContext = ExpressionUtils.createStandardEvaluationContext();
    }
  }

  @Override
  public String getComponentType() {
    return "ironmq:inbound-channel-adapter";
  }

  public void setQueueNameExpression(Expression qnExpression) {
    this.queueNameExpression = qnExpression;
  }

  public Message<Serializable> receive() {
    final String queueName = queueNameExpression.getValue(evaluationContext, String.class);
    final Client client = clientFactory.getClient();
    if (client == null) {
      logger.warn("Unable to obtain client from factory " + clientFactory);
      return null;
    }
    final Queue q = client.queue(queueName);
    if (q == null) {
      logger.warn("Unable to obtain queue " + queueName + " from client " + client);
      return null;
    }
    try {

      io.iron.ironmq.Messages messages = q.reserve(1, getReservationInSeconds());
      if (messages.getSize() == 0) {
        return null;
      }
      final io.iron.ironmq.Message message = messages.getMessage(0);

      MessageBuilder b = MessageBuilder.withPayload(extractPayload ? message.getBody() : message);
      b.setHeader(IronMqMessageHeaders.ID, message.getId());
      b.setHeader(IronMqMessageHeaders.RESERVATION_ID, message.getReservationId());
      try {
        b.setHeader(IronMqMessageHeaders.DELAY, message.getDelay());
      }
      catch (Exception e) {
      }
      try {
        b.setHeader(IronMqMessageHeaders.EXPIRES_IN, message.getExpiresIn());
      }
      catch (Exception e) {
      }
      try {
        b.setHeader(IronMqMessageHeaders.TIMEOUT, message.getTimeout());
      }
      catch (Exception e) {
      }
      final Message m = b.build();

      registerSyncOrComplete(q, message);

      return m;
    }
    catch (EmptyQueueException e) {
      return null;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * If we're in a transaction, register a transaction synchronization to delete the
   * message upon commit.
   * <p>
   * if we're not in a transaction, then delete the message from the queue upon consumption
   *
   * @param q the queue
   * @param message the message being registered
   * @throws IOException if message deletion fails
   */
  protected void registerSyncOrComplete(Queue q, io.iron.ironmq.Message message) throws IOException {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      logger.debug("Registering TransactionSynchronization for message id " + message.getId());
      TransactionSynchronizationManager.registerSynchronization(
        new IronMqMessageTransactionSynchronization(q, message));
      return;
    }

    logger.debug("TransactionSynchronization is not active, remove message from queue.");
    if (message.getReservationId() != null) {
      q.deleteMessage(message.getId(), message.getReservationId());
    }
    logger.debug("TransactionSynchronization is not active, successfully removed message from the queue.");
  }

  private final class IronMqMessageTransactionSynchronization extends TransactionSynchronizationAdapter {
    private final String messageId;
    private final Queue queue;

    IronMqMessageTransactionSynchronization(Queue queue, io.iron.ironmq.Message msg) {
      Assert.notNull(msg, "Message should not be null");
      Assert.notNull(queue, "Queue should not be null");
      this.messageId = msg.getId();
      this.queue = queue;
    }

    @Override
    public void afterCommit() {
      try {
        logger.debug("Transaction Committed, deleting message " + this.messageId);
        queue.deleteMessage(this.messageId);
        logger.debug("Transaction Committed, deleted message " + this.messageId + " successfully");
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

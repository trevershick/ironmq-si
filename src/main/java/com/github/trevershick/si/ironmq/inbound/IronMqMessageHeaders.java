package com.github.trevershick.si.ironmq.inbound;

public interface IronMqMessageHeaders {
  String PREFIX = "ironmq_";
  String DELAY = PREFIX + "delay";
  String EXPIRES_IN = PREFIX + "expiresIn";
  String ID = PREFIX + "id";
  String TIMEOUT = PREFIX + "timeout";
  String QUEUE = PREFIX + "queue";
  String RESERVATION_ID = "ironmq_reservation_id";
}

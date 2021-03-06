package com.github.trevershick.si.ironmq.config.xml;

public interface IronMqParserConstants {

  String PROPERTY_CLIENT_FACTORY = "clientFactory";
  String PROPERTY_QUEUE_NAME = "queueName";
  String PROPERTY_QUEUE_NAME_EXPRESSION = "queueNameExpression";
  String PROPERTY_EXTRACT_PAYLOAD = "extractPayload";
  String PROPERTY_RESERVATION_IN_SECONDS = "reservationInSeconds";

  String ATTRIBUTE_PROJECT_ID = "ironmq-project";
  String ATTRIBUTE_TOKEN = "ironmq-token";
  String ATTRIBUTE_CLOUD = "ironmq-cloud-url";
  String ATTRIBUTE_CLIENT_FACTORY = "client-factory";
  String ATTRIBUTE_RESERVATION_IN_SECONDS = "reservation-in-seconds";
  String ATTRIBUTE_QUEUE_NAME_EXPRESSION = "queue-name-expression";
  String ATTRIBUTE_QUEUE_NAME = "queue-name";
  String ATTRIBUTE_EXTRACT_PAYLOAD = "extract-payload";
}

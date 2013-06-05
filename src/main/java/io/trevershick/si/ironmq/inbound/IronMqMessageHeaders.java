package io.trevershick.si.ironmq.inbound;

public interface IronMqMessageHeaders {
	final String PREFIX = "ironmq_";
	final String DELAY = PREFIX + "delay";
	final String EXPIRES_IN = PREFIX + "expiresIn";
	final String ID = PREFIX + "id";
	final String TIMEOUT = PREFIX + "timeout";
}

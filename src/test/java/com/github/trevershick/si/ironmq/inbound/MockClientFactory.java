package com.github.trevershick.si.ironmq.inbound;

import static org.mockito.Mockito.mock;

import com.github.trevershick.si.ironmq.IronMqClientFactory;

import io.iron.ironmq.Client;

public class MockClientFactory implements IronMqClientFactory {

	final Client mock = mock(Client.class);
	
	public Client getClient() {
		return mock;
	}
}

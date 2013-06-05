package io.trevershick.si.ironmq;

import io.iron.ironmq.Client;

public interface IronMqClientFactory {
	Client getClient();
}

package com.github.trevershick.si.ironmq;

import io.iron.ironmq.Client;

public interface IronMqClientFactory {
	Client getClient();
}

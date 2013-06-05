package com.github.trevershick.si.ironmq;

import io.iron.ironmq.Client;

import org.springframework.beans.factory.InitializingBean;

public class DefaultIronMqClientFactory implements IronMqClientFactory, InitializingBean {

	private volatile Client client;
	private final String projectId;
	private final String token;
	
	public DefaultIronMqClientFactory(String projectId, String token) {
		this.projectId = projectId;
		this.token = token;
	}

	public Client getClient() {
		return client;
	}

	public void afterPropertiesSet() throws Exception {
		client = new Client(projectId, token);
	}

	public String getProjectId() {
		return projectId;
	}

	public String getToken() {
		return token;
	}

	
}

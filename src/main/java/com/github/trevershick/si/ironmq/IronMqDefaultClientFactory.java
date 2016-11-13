package com.github.trevershick.si.ironmq;

import io.iron.ironmq.Client;
import io.iron.ironmq.Cloud;

import java.net.MalformedURLException;

import org.springframework.beans.factory.InitializingBean;

public class IronMqDefaultClientFactory implements IronMqClientFactory, InitializingBean {

	private volatile Client client;
	private final String projectId;
	private final String token;
	private final String cloud;

	public IronMqDefaultClientFactory(String projectId, String token, String cloud) {
		this.projectId = projectId;
		this.token = token;
		this.cloud = cloud;
	}

	public Client getClient() {
		return client;
	}

	public void afterPropertiesSet() throws Exception {
		client = new Client(projectId, token, cloud());
	}

	private Cloud cloud() throws MalformedURLException {
		if (this.cloud == null) { return null; }
		return new Cloud(cloud);
	}

	public String getCloud() {
		return cloud;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getToken() {
		return token;
	}

	
}

package com.newrelic.fit.plugins.messagebroker;

public class ConnectionInfo {
	private String name;
	private String host;
	private int port;
	private boolean login;
	private String user;
	private String password;
	private String queueManager;
	private String channel;
	private String topicNameResourceStatistics;
	private String topicNameStatisticsAccounting;
	
	public String getQueueManager() {
		return queueManager;
	}
	public void setQueueManager(String queueManager) {
		this.queueManager = queueManager;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getTopicNameResourceStatistics() {
		return topicNameResourceStatistics;
	}
	public void setTopicNameResourceStatistics(String topicNameResourceStatistics) {
		this.topicNameResourceStatistics = topicNameResourceStatistics;
	}
	public String getTopicNameStatisticsAccounting() {
		return topicNameStatisticsAccounting;
	}
	public void setTopicNameStatisticsAccounting(String topicNameStatisticsAccounting) {
		this.topicNameStatisticsAccounting = topicNameStatisticsAccounting;
	}

	private String baseMetricPath = "";
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBaseMetricPath() {
		return baseMetricPath;
	}
	public void setBaseMetricPath(String baseMetricPath) {
		this.baseMetricPath = baseMetricPath;
	}
	public boolean isLogin() {
		return login;
	}
	public void setLogin(boolean login) {
		this.login = login;
	}
	
}

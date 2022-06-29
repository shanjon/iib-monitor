package com.newrelic.fit.plugins.messagebroker;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;

public class WMBAgentFactory extends AgentFactory {
	
	private static final Logger logger = Logger.getLogger(WMBAgentFactory.class);
	
	private static final int DEFAULT_PORT = 1414;
	private Map<String, MetricTypeDef> resourceMetricTypesMap = null;
	private Map<String, MetricTypeDef> flowMetricTypesMap = null;
	private String GUID = null;
	
	public WMBAgentFactory(String guid, Map<String, MetricTypeDef> resourceMetricTypesMap, Map<String, MetricTypeDef> flowMetricTypesMap) {
		this.resourceMetricTypesMap  = resourceMetricTypesMap;
		this.flowMetricTypesMap  = flowMetricTypesMap;
		this.GUID  = guid;
	}

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		String name = (String) properties.get("name");
		logger.info("Creating agent for " + name);
		String host = (String) properties.get("host");
		Long port = (Long) properties.get("port");
		if(port == null) {
			port = new Long(DEFAULT_PORT);
		}
		boolean login = (Boolean) properties.get("login");
		String username = (String) properties.get("username");
		String password = (String) properties.get("password");
		String queueManager = (String) properties.get("queueManager");
		String channel = (String) properties.get("channel");
		String topicNameResourceStatistics = (String) properties.get("topicNameResourceStatistics");
		String topicNameStatisticsAccounting = (String) properties.get("topicNameStatisticsAccounting");
		String baseMetricPath = (String) properties.get("baseMetricPath");
		
		ConnectionInfo conn = new ConnectionInfo();
		conn.setName(name);
		conn.setHost(host);
		conn.setPort(port.intValue());
		conn.setLogin(login);
		conn.setUser(username);
		conn.setPassword(password);
		conn.setQueueManager(queueManager);
		if ((topicNameResourceStatistics != null)) {
			conn.setTopicNameResourceStatistics(topicNameResourceStatistics);
		} else {
			conn.setTopicNameResourceStatistics(null);
		}
		if ((topicNameStatisticsAccounting != null)) {
			conn.setTopicNameStatisticsAccounting(topicNameStatisticsAccounting);
		} else {
			conn.setTopicNameStatisticsAccounting(null);
		}
		
		conn.setChannel(channel);
		conn.setBaseMetricPath(baseMetricPath);
		return new WMBAgent(GUID, conn, resourceMetricTypesMap, flowMetricTypesMap);
	}

}

package com.newrelic.fit.plugins.messagebroker;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import com.ibm.mq.jms.MQTopicConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;

public class WMBAgent extends Agent {
	private static final Logger logger = Logger.getLogger(Agent.class);
	private static final String version = "1.0.0";
	private ConnectionInfo config;
	private String name;
	private boolean resourceStatisticsInitialized = false;
	private boolean accountingStatisticsInitialized = false;
	
	MQTopicConnectionFactory topicConnectionFactory = null;
	
	TopicConnection topicResourceStatisticsConnection = null;
	TopicSession topicResourceStatisticsSession = null;
	TopicSubscriber topicResourceStatisticsSubscriber = null;
	
	TopicConnection topicAccountingStatisticsConnection = null;
	TopicSession topicAccountingStatisticsSession = null;
	TopicSubscriber topicAccountingStatisticsSubscriber = null;
	
	private Map<String, MetricTypeDef> resourceMetricTypesMap = null;
	private Map<String, MetricTypeDef> flowMetricTypesMap = null;
	
	public WMBAgent(String GUID, ConnectionInfo conn, Map<String, MetricTypeDef> resourceMetricTypesMap, Map<String, MetricTypeDef> flowMetricTypesMap) {
		super(GUID, version);
		this.config = conn;
		this.resourceMetricTypesMap  = resourceMetricTypesMap;
		this.flowMetricTypesMap  = flowMetricTypesMap;
		name = conn.getName();
	}
	
	@Override
	public String getAgentName() {
		return name;
	}
	
	public Map<String, MetricTypeDef> getResourceMetricTypesMap() {
		return resourceMetricTypesMap;
	}
	
	public Map<String, MetricTypeDef> getFlowMetricTypesMap() {
		return flowMetricTypesMap ;
	}
	
	private boolean initResourceStatistics() {
		logger.info("Initializing connection to websphere MQ [" + config.getHost() + ":" + config.getPort() + "/qm=" +config.getQueueManager()
				+ "channel=" + config.getChannel());
		try {
			topicConnectionFactory = new MQTopicConnectionFactory();

			topicConnectionFactory.setHostName(config.getHost());
			topicConnectionFactory.setPort(config.getPort());
			topicConnectionFactory.setQueueManager(config.getQueueManager());
			topicConnectionFactory.setChannel(config.getChannel());
			topicConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
			topicConnectionFactory.setClientReconnectOptions(WMQConstants.WMQ_CLIENT_RECONNECT);
		} catch (Exception e) {
			logger.error("Could not create MQ Connection Factory" + e.toString(), e);
			resourceStatisticsInitialized = false;
			return false;
		}
		if (config.getTopicNameResourceStatistics() != null) {
			logger.info("Initializing websphere MQ topic session[ topic-name-resource-statistics= " + config.getTopicNameResourceStatistics() + "]");
			try {
				if (config.isLogin()) {
					topicResourceStatisticsConnection = topicConnectionFactory.createTopicConnection(config.getUser(), config.getPassword());
				} else {
					topicResourceStatisticsConnection = topicConnectionFactory.createTopicConnection();
				}
				topicResourceStatisticsConnection.setClientID("nr-resource-client");
				topicResourceStatisticsSession = topicResourceStatisticsConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			} catch (Exception e) {
				e.printStackTrace();
				if (topicResourceStatisticsConnection != null) {
					try {
						topicResourceStatisticsConnection.close();
					} catch (JMSException ee) {
						e.printStackTrace();
					}
				}
				logger.error("Could not create Topic Session " + e.toString(), e);
				resourceStatisticsInitialized = false;
				return false;
			}
			
			logger.info("Subscribing to websphere MQ topic | " + config.getTopicNameResourceStatistics());
			try {
				Topic topicResourceStatistics = topicResourceStatisticsSession.createTopic(config.getTopicNameResourceStatistics().trim());
				topicResourceStatisticsSubscriber = topicResourceStatisticsSession.createSubscriber(topicResourceStatistics);
				topicResourceStatisticsSubscriber.setMessageListener(new ResourceStatisticsListener());
				
				topicResourceStatisticsConnection.setExceptionListener(new ExceptionListener() {

					@Override
					public void onException(JMSException ex) {
						cleanupResourceStatistics();
						resourceStatisticsInitialized = false;
					}
					
				});
				topicResourceStatisticsConnection.start();
			} catch (Exception e) {
				logger.error("Could not subscribe to Topic" + e.toString(), e);
				if (topicResourceStatisticsConnection != null) {
					try {
						topicResourceStatisticsConnection.close();
					} catch (JMSException ee) {
						e.printStackTrace();
					}
				}
				resourceStatisticsInitialized = false;
				return false;
			}
			logger.info("Successfully subscribed to topic = " + config.getTopicNameResourceStatistics());
		}
		resourceStatisticsInitialized = true;
		return true;
	}
	
	public boolean initStatisticsAccounting() {
		if (config.getTopicNameStatisticsAccounting() != null) {
			logger.info("Initializing websphere MQ topic session[ topic-name-accounting-statistics= " + config.getTopicNameStatisticsAccounting() + "]");
			try {
				if (config.isLogin()) {
					topicAccountingStatisticsConnection = topicConnectionFactory.createTopicConnection(config.getUser(), config.getPassword());
				} else {
					topicAccountingStatisticsConnection = topicConnectionFactory.createTopicConnection();
				}
				topicAccountingStatisticsConnection.setClientID("nr-stats-client");
				topicAccountingStatisticsSession = topicAccountingStatisticsConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
	
			} catch (Exception e) {
				e.printStackTrace();
				if (topicAccountingStatisticsConnection != null) {
					try {
						topicAccountingStatisticsConnection.close();
					} catch (JMSException ee) {
						e.printStackTrace();
					}
				}
				logger.error("Could not create Topic Session " + e.toString(), e);
				accountingStatisticsInitialized = false;
				return false;
			}
			
			logger.info("Subscribing to websphere MQ topic | " + config.getTopicNameStatisticsAccounting());
			try {
				Topic topicAccountingStatistics = topicAccountingStatisticsSession.createTopic(config.getTopicNameStatisticsAccounting().trim());
				topicAccountingStatisticsSubscriber = topicAccountingStatisticsSession.createSubscriber(topicAccountingStatistics);
				topicAccountingStatisticsSubscriber.setMessageListener(new AccountingStatisticsListener());
				
				topicAccountingStatisticsConnection.setExceptionListener(new ExceptionListener() {

					@Override
					public void onException(JMSException ex) {
						cleanupAccountingStatistics();
						accountingStatisticsInitialized = false; 
					}
					
				});
				topicAccountingStatisticsConnection.start();

				
				
			} catch (Exception e) {
				logger.error("Could not subscribe to Topic" + e.toString(), e);
				if (topicAccountingStatisticsConnection != null) {
					try {
						topicAccountingStatisticsConnection.close();
					} catch (JMSException ee) {
						e.printStackTrace();
					}
				}
				accountingStatisticsInitialized = false;
				return false;
			}
			logger.info("Successfully subscribed to topic = " + config.getTopicNameStatisticsAccounting());
		}
		
		accountingStatisticsInitialized = true;
		return true;
	}
	
	private void cleanupAccountingStatistics() {
		try {
			topicAccountingStatisticsSubscriber.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicAccountingStatisticsSession.unsubscribe(config.getTopicNameResourceStatistics());
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicAccountingStatisticsConnection.stop();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicAccountingStatisticsConnection.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void cleanupResourceStatistics() {
		try {
			topicResourceStatisticsSubscriber.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicResourceStatisticsSession.unsubscribe(config.getTopicNameResourceStatistics());
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicResourceStatisticsConnection.stop();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			topicResourceStatisticsConnection.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void pollCycle() {
		
		//logger.info("starting poll cycle");
		if (resourceStatisticsInitialized == false) {
			this.initResourceStatistics();
		}
		if (accountingStatisticsInitialized == false) {
			this.initStatisticsAccounting();
		}
	}

	
	public class ResourceStatisticsListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			//logger.debug("Resource statistics message received");
			if (message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				try {
					parseResourceStatistics(msg.getText());
				} catch (JMSException e) {
					logger.error("Error parsing resource message " + e.toString(), e);
				}
			} else {
				//error?
			}
		}
	}
	
	
	public void parseResourceStatistics(InputStream inStream) {
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         ResourceStatsHandler statshandler = new ResourceStatsHandler(this);
	         saxParser.parse(inStream, statshandler);     
	      } catch (Exception e) {
	    	  logger.error("Error parsing message " + e.toString(), e);
	      }
	}
	
	public void parseResourceStatistics(String xmlText) {
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         ResourceStatsHandler statshandler = new ResourceStatsHandler(this);
	         saxParser.parse(new InputSource(new StringReader(xmlText)), statshandler);     
	      } catch (Exception e) {
	    	  logger.error("Error parsing message " + e.toString(), e);
	      }
	}
	

	public class AccountingStatisticsListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			//logger.debug("Accounting stats message received");
			if (message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				try {
					//logger.info(msg.getText());
					parseAccountingStatistics(msg.getText());
				} catch (JMSException e) {
					logger.error(e.toString(), e);
				}
			} else {
				//error?
			}
		}
	}
	
	public void parseAccountingStatistics(InputStream inStream) {
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         AccountingStatsHandler statshandler = new AccountingStatsHandler(this);
	         saxParser.parse(inStream, statshandler);     
	      } catch (Exception e) {
	    	  	logger.error(e.toString(), e);
	      }
	}
	
	public void parseAccountingStatistics(String xmlText) {
		try {	
	         SAXParserFactory factory = SAXParserFactory.newInstance();
	         SAXParser saxParser = factory.newSAXParser();
	         AccountingStatsHandler statshandler = new AccountingStatsHandler(this);
	         saxParser.parse(new InputSource(new StringReader(xmlText)), statshandler);     
	      } catch (Exception e) {
	    	  	logger.error(e.toString(), e);
	      }
	}
}

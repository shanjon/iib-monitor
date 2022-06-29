package com.newrelic.fit.plugins.messagebroker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.newrelic.fit.plugins.common.processors.MetricProcessingException;

public class AccountingStatsHandler extends DefaultHandler {

	private static final Logger logger = Logger.getLogger(AccountingStatsHandler.class.getName());
	
	private Map<String, MetricTypeDef> metricTypesMap = null;
	private WMBAgent wmbAgent = null;

	public AccountingStatsHandler(WMBAgent wmbAgent) {
		super();
		this.wmbAgent  = wmbAgent;
		this.metricTypesMap = wmbAgent.getFlowMetricTypesMap();
	}

	@Override 
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("MessageFlow")) {
			String brokerLabel  = attributes.getValue("BrokerLabel");
			String executionGroupName = attributes.getValue("ExecutionGroupName");
			String messageFlowName  = attributes.getValue("MessageFlowName");
			
			/*
			for (int i = 0; i < attributes.getLength(); i++) {
				String lattributeName = attributes.getLocalName(i);
				String attributeName = attributes.getQName(i);
				String attributeValue = attributes.getValue(i);
			}
			*/
			

			MetricTypeDef metricTypeDef = metricTypesMap.get("MessageFlow");
			if (metricTypeDef != null) {
				String insightsEventType = metricTypeDef.getEventSubType();
				Map<String, Object> metricset = new HashMap<String, Object>();
				metricset.put("eventType", "IIBSample");
				metricset.put("eventSubType", insightsEventType);
				
				metricset.put("instanceName", wmbAgent.getAgentName());
				metricset.put("brokerLabel", brokerLabel);
				metricset.put("executionGroupName", executionGroupName);
				metricset.put("messageFlowName", messageFlowName);
				
				for (int i = 0; i < attributes.getLength(); i++) {
				    String attributeName = attributes.getQName(i);
				    String attributeValue = attributes.getValue(i);
				    MetricDef metric = metricTypeDef.getMetricDef(attributeName);
				    if (metric != null) {
				    		Number metricNumericValue = 0;
						try {
							metricNumericValue = metric.getProcessor().process(attributeValue);
							metricset.put(metric.getDisplayName(), metricNumericValue);
						} catch (MetricProcessingException e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
							//System.out.println(e.getMessage());
						}
				    } else {
				    		//metricset.put(metric.getDisplayName(), attributeValue);
				    }
				}
				wmbAgent.reportMetricSet(metricset);
			} else {
				logger.severe("No metric types definition found for \'MessageFlow\'. Metrics will not be reported for this type.") ;
			}
		} else if (qName.equalsIgnoreCase("Threads")) {
			MetricTypeDef metricTypeDef = metricTypesMap.get("Threads");
			if (metricTypeDef != null) {
				String insightsEventType = metricTypeDef.getEventSubType();
				Map<String, Object> metricset = new HashMap<String, Object>();
				metricset.put("eventType", "IIBSample");
				metricset.put("eventSubType", insightsEventType);
				
				metricset.put("instanceName", wmbAgent.getAgentName());
				
				for (int i = 0; i < attributes.getLength(); i++) {
				    String attributeName = attributes.getQName(i);
				    String attributeValue = attributes.getValue(i);
				    MetricDef metric = metricTypeDef.getMetricDef(attributeName);
				    if (metric != null) {
				    		Number metricNumericValue = 0;
						try {
							metricNumericValue = metric.getProcessor().process(attributeValue);
							metricset.put(metric.getDisplayName(), metricNumericValue);
						} catch (MetricProcessingException e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
				    } else {
				    		//metricset.put(metric.getDisplayName(), attributeValue);
				    }
				}
				wmbAgent.reportMetricSet(metricset);
			} else {
				logger.severe("No metric types definition found for \'Threads\'. Metrics will not be reported for this type.") ;
			}
		} else if (qName.equalsIgnoreCase("ThreadStatistics")) {
			MetricTypeDef metricTypeDef = metricTypesMap.get("ThreadStatistics");
			if (metricTypeDef != null) {
				String insightsEventType = metricTypeDef.getEventSubType();
				Map<String, Object> metricset = new HashMap<String, Object>();
				metricset.put("eventType", "IIBSample");
				metricset.put("eventSubType", insightsEventType);
				
				metricset.put("instanceName", wmbAgent.getAgentName());
				
				for (int i = 0; i < attributes.getLength(); i++) {
				    String attributeName = attributes.getQName(i);
				    String attributeValue = attributes.getValue(i);
				    MetricDef metric = metricTypeDef.getMetricDef(attributeName);
				    if (metric != null) {
				    		Number metricNumericValue = 0;
						try {
							metricNumericValue = metric.getProcessor().process(attributeValue);
							metricset.put(metric.getDisplayName(), metricNumericValue);
						} catch (MetricProcessingException e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
				    } else {
				    		//metricset.put(metric.getDisplayName(), attributeValue);
				    }
				}
				wmbAgent.reportMetricSet(metricset);
			} else {
				logger.severe("No metric types definition found for \'ThreadStatistics\'. Metrics will not be reported for this type.") ;
			}
		} else {
			MetricTypeDef metricTypeDef = metricTypesMap.get(qName);
			if (metricTypeDef != null) {
				String insightsEventType = metricTypeDef.getEventSubType();
				Map<String, Object> metricset = new HashMap<String, Object>();
				metricset.put("eventType", "IIBSample");
				metricset.put("eventSubType", insightsEventType);
				
				metricset.put("instanceName", wmbAgent.getAgentName());
				
				for (int i = 0; i < attributes.getLength(); i++) {
				    String attributeName = attributes.getQName(i);
				    String attributeValue = attributes.getValue(i);
				    MetricDef metric = metricTypeDef.getMetricDef(attributeName);
				    if (metric != null) {
				    		Number metricNumericValue = 0;
						try {
							metricNumericValue = metric.getProcessor().process(attributeValue);
							metricset.put(metric.getDisplayName(), metricNumericValue);
						} catch (MetricProcessingException e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
				    } else {
				    		//metricset.put(metric.getDisplayName(), attributeValue);
				    }
				}
				wmbAgent.reportMetricSet(metricset);
			} else {
				//logger.info("No metric types definition found for \'"+ qName + "\'. Metrics will not be reported for this type.") ;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}	
}

package com.newrelic.fit.plugins.messagebroker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.newrelic.fit.plugins.common.processors.MetricProcessingException;

public class ResourceStatsHandler extends DefaultHandler {

	private static final Logger logger = Logger.getLogger(ResourceStatsHandler.class.getName());
	
	private String brokerLabel = "";
	private String executionGroupName = "";
	private String resourceType = "";
	private String resourceIdentifier = "";
	private WMBAgent wmbAgent = null;

	private Map<String, MetricTypeDef> metricTypesMap;

	public ResourceStatsHandler(WMBAgent wmbAgent) {
		super();
		this.wmbAgent  = wmbAgent;
		this.metricTypesMap = wmbAgent.getResourceMetricTypesMap();
	}

	@Override 
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("ResourceStatistics")) {
			brokerLabel  = attributes.getValue("brokerLabel");
			executionGroupName = attributes.getValue("executionGroupName");
		} else if (qName.equalsIgnoreCase("ResourceType")) {
			resourceType  = attributes.getValue("name");
		} else if (qName.equalsIgnoreCase("resourceIdentifier")) {
			resourceIdentifier = attributes.getValue("name");
			String resourceTypeId = resourceType + "/" + resourceIdentifier ;
			
			MetricTypeDef metricTypeDef = metricTypesMap.get(resourceTypeId);
			if (metricTypeDef != null) {
				String insightsEventType = metricTypeDef.getEventSubType();
				Map<String, Object> metricset = new HashMap<String, Object>();
				metricset.put("eventType", "IIBSample");
				metricset.put("eventSubType", insightsEventType);
				metricset.put("instanceName", wmbAgent.getAgentName());
				metricset.put("brokerLabel", brokerLabel);
				metricset.put("executionGroupName", executionGroupName);
				metricset.put("resourceType", resourceType);
				metricset.put("resourceIdentifier", resourceIdentifier);
				
				for (int i = 0; i < attributes.getLength(); i++) {
				    String attributeName = attributes.getLocalName(i);
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
				    		metricset.put(attributeName, attributeValue);
				    }
				    //System.out.println("**" + brokerLabel+ "/" + executionGroupName + "/" + resourceTypeId + "/" + attributeName + "=" + attributeValue);
				}
				/*
				for (MetricDef metricDef: metricTypeDef.getMetrics()) {
					String metricName = metricDef.getName();
					String metricPath = metricTypeDef.getMetricPath() + "/" + brokerLabel+ "/" + executionGroupName + "/" + metricName;
					try {
						String metricValue = attributes.getValue(metricName);
						Number metricNumericValue = metricDef.getProcessor().process(metricValue);
						//logger.info(metricPath + "[" +  metricDef.getUnit() + "] = " + metricNumericValue);
						//reportMetric(metricPath, metricDef.getUnit(), metricNumericValue);

					} catch (MetricProcessingException e) {
						logger.log(Level.SEVERE, "Error [MetricProcessingException] processing metric [" + metricPath + "]. " + e.toString(), e);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Error reporting metric [" + metricPath + "]. " + e.toString(), e);
					}
				}
				*/
				wmbAgent.reportMetricSet(metricset);
			}			
		} else {
			//System.out.println("Unexpected Element: " + qName);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}	
}

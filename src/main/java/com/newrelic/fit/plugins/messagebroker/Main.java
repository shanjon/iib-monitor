package com.newrelic.fit.plugins.messagebroker;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.newrelic.fit.plugins.common.processors.OpStateProcessor;
import com.newrelic.fit.plugins.common.processors.StringToFloatProcessor;
import com.newrelic.fit.plugins.common.processors.StringToIntegerProcessor;
import com.newrelic.fit.plugins.common.processors.StringToLongProcessor;
import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) {
		String configDir = System.getProperty("newrelic.platform.config.dir");
		if (configDir == null) {
			configDir = "./config";
		}
		
		String newrelicConfFile = configDir + File.separatorChar + "newrelic.json";
		String guid = readProductGUID(newrelicConfFile);
		if (guid == null) {
			guid = "com.newrelic.expertservices.websphere.messagebroker";
		}
		
		String flowMetricTypesFileName = configDir + File.separatorChar + "flow-metric-types.json";
		File flowMetricTypesFile = new File(flowMetricTypesFileName);
		if (!flowMetricTypesFile.exists()) {
			logger.error("flow metric types file does not exist. Ensure that file [" + flowMetricTypesFile.getAbsolutePath() + "] exists");
			System.exit(-1);
		}
		Map<String, MetricTypeDef> flowMetricTypesMap = getFlowMetricTypes(flowMetricTypesFile);
		logger.info("Flow metrics types read: ");
		Iterator<Map.Entry<String, MetricTypeDef>> it1 = flowMetricTypesMap.entrySet().iterator();
	    while (it1.hasNext()) {
	        Map.Entry<String, MetricTypeDef> pair = it1.next();
	        logger.info(pair.getKey() + " = " + pair.getValue());
	    }
		
		String resourceMetricTypesFileName = configDir + File.separatorChar + "resource-metric-types.json";
		File resourceMetricTypesFile = new File(resourceMetricTypesFileName);
		if (!resourceMetricTypesFile.exists()) {
			logger.error("resource metric types file does not exist. Ensure that file [" + resourceMetricTypesFile.getAbsolutePath() + "] exists");
			System.exit(-1);
		}
		Map<String, MetricTypeDef> resourceMetricTypesMap = getResourceMetricTypes(resourceMetricTypesFile);
		logger.info("Resource metrics types read: ");
		Iterator<Map.Entry<String, MetricTypeDef>> it2 = resourceMetricTypesMap.entrySet().iterator();
	    while (it2.hasNext()) {
	        Map.Entry<String, MetricTypeDef> pair = it2.next();
	        logger.info(pair.getKey() + " = " + pair.getValue());
	    }
	    
		try {
			Runner runner = new Runner();
			runner.add(new WMBAgentFactory(guid, resourceMetricTypesMap, flowMetricTypesMap));
			runner.setupAndRun();
		} catch (ConfigurationException e) {
			System.err.println("ERROR: "+e.getMessage());
			System.exit(-1);
		}
	}
	
	public static Map<String, MetricTypeDef> getFlowMetricTypes(File metricTypesFile) {
		Config config = ConfigFactory.parseFile(metricTypesFile);
		Map<String, MetricTypeDef> metricTypesMap = new HashMap<String, MetricTypeDef>();
		for (Config c : config.getConfigList("MetricTypes")) {
			MetricTypeDef metricTypeDef = new MetricTypeDef();
			String resourceTypeId = c.getString("ElementType");
			metricTypeDef.setMetricPath(c.getString("metricPath"));
			metricTypeDef.setEventSubType(c.getString("eventSubType"));
			
			for (Config metric : c.getConfigList("metrics")) {
				MetricDef metricDef = new MetricDef();
				metricDef.setName(metric.getString("metric"));
				String processor = metric.getString("processor");
				if (processor.equalsIgnoreCase("IntProcessor")) {
					metricDef.setProcessor(new StringToIntegerProcessor());
				} else if (processor.equalsIgnoreCase("FloatProcessor")) {
					metricDef.setProcessor(new StringToFloatProcessor());
				} else if (processor.equalsIgnoreCase("LongProcessor")) {
					metricDef.setProcessor(new StringToLongProcessor());
				} else if (processor.equalsIgnoreCase("OpStateProcessor")) {
					metricDef.setProcessor(new OpStateProcessor());
				} else {
					System.out.println("Invalid processor type: " + processor);
					System.exit(1);
				}
				metricDef.setUnit(metric.getString("unit"));
				metricDef.setDisplayName(metricTypeDef.getEventSubType() + "." + metricDef.getName());
				metricTypeDef.add(metricDef);
			}
			metricTypesMap.put(resourceTypeId, metricTypeDef);
		}
		return metricTypesMap;
	}
	
	public static Map<String, MetricTypeDef> getResourceMetricTypes(File metricTypesFile) {
		logger.info("Parsing file " + metricTypesFile.getAbsolutePath());
		Config config = ConfigFactory.parseFile(metricTypesFile);
		Map<String, MetricTypeDef> metricTypesMap = new HashMap<String, MetricTypeDef>();
		for (Config c : config.getConfigList("MetricTypes")) {
			MetricTypeDef metricTypeDef = new MetricTypeDef();
			String resourceTypeId = c.getString("resourceType/Id");
			metricTypeDef.setMetricPath(c.getString("metricPath"));
			metricTypeDef.setEventSubType(c.getString("eventSubType"));
			
			for (Config metric : c.getConfigList("metrics")) {
				MetricDef metricDef = new MetricDef();
				metricDef.setName(metric.getString("metric"));
				String processor = metric.getString("processor");
				if (processor.equalsIgnoreCase("IntProcessor")) {
					metricDef.setProcessor(new StringToIntegerProcessor());
				} else if (processor.equalsIgnoreCase("FloatProcessor")) {
					metricDef.setProcessor(new StringToFloatProcessor());
				} else if (processor.equalsIgnoreCase("LongProcessor")) {
					metricDef.setProcessor(new StringToLongProcessor());
				} else if (processor.equalsIgnoreCase("OpStateProcessor")) {
					metricDef.setProcessor(new OpStateProcessor());
				} else {
					logger.error("Invalid processor type: " + processor);
					System.exit(1);
				}
				metricDef.setUnit(metric.getString("unit"));
				metricDef.setDisplayName(metricTypeDef.getEventSubType() + "." + metricDef.getName());
				metricTypeDef.add(metricDef);
			}
			metricTypesMap.put(resourceTypeId, metricTypeDef);
		}
		return metricTypesMap;
	}
	
	public static String readProductGUID(String newrelicConfFile) {
		logger.info("Parsing file " + newrelicConfFile);
		Config config = ConfigFactory.parseFile(new File(newrelicConfFile));
		String guid = config.getString("guid");
		return guid;
	}
}

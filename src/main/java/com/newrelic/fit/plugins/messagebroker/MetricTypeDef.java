package com.newrelic.fit.plugins.messagebroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricTypeDef {

	private String metricPath;
	private String eventSubType;
	private Map<String, MetricDef> metricMap = new HashMap<String, MetricDef>();
	private List<MetricDef> metricList = new ArrayList<MetricDef>();
	
	public String getMetricPath() {
		return metricPath;
	}
	public void setMetricPath(String metricPath) {
		this.metricPath = metricPath;
	}
	
	public void add(MetricDef metricDef) {
		metricMap.put(metricDef.getName(), metricDef);
		metricList.add(metricDef);
	}
	
	public MetricDef getMetricDef(String metricName) {
		return metricMap.get(metricName);
	}
	
	public List<MetricDef> getMetrics() {
		return metricList;
	}
	public String getEventSubType() {
		return eventSubType;
	}
	public void setEventSubType(String eventSubType) {
		this.eventSubType = eventSubType;
	}
	@Override
	public String toString() {
		return "MetricTypeDef [metricPath=" + metricPath + ", eventSubType=" + eventSubType + ", metricMap=" + metricMap
				+ ", metricList=" + metricList + "]";
	}
	
}

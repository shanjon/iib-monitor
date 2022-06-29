package com.newrelic.fit.plugins.messagebroker;

import com.newrelic.fit.plugins.common.processors.MetricProcessor;

public class MetricDef {

	String name;
	String unit;
	MetricProcessor processor;
	String displayName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public MetricProcessor getProcessor() {
		return processor;
	}
	public void setProcessor(MetricProcessor processor) {
		this.processor = processor;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}

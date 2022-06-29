package com.newrelic.fit.plugins.common.processors;

public interface MetricProcessor {
	public Number process(String value) throws MetricProcessingException;
}

package com.newrelic.fit.plugins.common.processors;

public class StringToIntegerProcessor implements MetricProcessor {

	@Override
	public Number process(String value) throws MetricProcessingException {
		try {
			Integer i = Integer.parseInt(value);
			return i;
		} catch (Exception e) {
			throw new MetricProcessingException(e);
		}
	}
}

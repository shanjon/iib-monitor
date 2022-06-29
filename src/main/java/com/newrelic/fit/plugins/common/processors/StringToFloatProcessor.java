package com.newrelic.fit.plugins.common.processors;

public class StringToFloatProcessor implements MetricProcessor {

	@Override
	public Number process(String value) throws MetricProcessingException {
		try {
			Float i = Float.parseFloat(value);
			return i;
		} catch (Exception e) {
			throw new MetricProcessingException(e);
		}
	}

}

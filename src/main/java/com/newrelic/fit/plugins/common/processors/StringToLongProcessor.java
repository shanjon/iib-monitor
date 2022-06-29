package com.newrelic.fit.plugins.common.processors;

public class StringToLongProcessor implements MetricProcessor {

	@Override
	public Number process(String value) throws MetricProcessingException {
		try {
			Long i = Long.parseLong(value);
			return i;
		} catch (Exception e) {
			throw new MetricProcessingException(e);
		}
	}

}

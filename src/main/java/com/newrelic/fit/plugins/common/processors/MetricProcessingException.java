package com.newrelic.fit.plugins.common.processors;

public class MetricProcessingException extends Exception {

	private static final long serialVersionUID = -33310284212696058L;

	public MetricProcessingException(Exception e) {
		super(e);
	}

}

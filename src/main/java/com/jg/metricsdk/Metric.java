package com.jg.metricsdk;

import com.jg.metricsdk.model.MetricUnit;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Metric implements ApplicationContextAware {

	public static MetricBuilder key (String key) {
		return new MetricBuilder(key);
	}

	static void process (MetricUnit metricDto) {
		metricUpdater.sendMetric(metricDto);
	}

	private static MetricUpdater metricUpdater;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		metricUpdater = applicationContext.getBean(MetricUpdater.class);
	}
}
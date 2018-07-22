package com.jg.metricsdk;

import com.jg.metricsdk.model.MetricUnit;

/**
 * - How to use metric SDK - (v.0.0.1)
 *
 * 1. Please add following properties on metric to your spring project
 *	metric.basic.path	: basic metric key
 *	metric.tx.mode		: http, kafka
 *	metric.http.host	: for http tx mode, http host url
 *	metric.kafka.host	: for kafka tx mode, kafka host
 *	metric.kafka.topic	: for kafka tx mode, kafka topic
 *
 *	cf. structure of metric key is as follows: (basic metric path).metric.(metric key)
 *
 * 2. Import MetricConfig.class to your project
 * 	add @EnableMetricSdk to main application (for spring boot) or configuration class (for spring)
 *
 * 3. To use metric updater, add '@Autowired private MetricUpdater metricUpdater' in a class
 */

public interface MetricUpdater {

	/**
	 * (1) count metric: accumulated value in a minute is calculated.
	 *  ex.
	 *		Long count = list.stream().filter(...).count();
	 *		metricUpdater.count("example", count);
	 *
	 * @param key
	 * @param count
	 */
	void count(String key, Long count);

	/**
	 * (1) count metric: accumulated value in a minute is calculated.
	 * @param key
	 * @param count
	 */
	void count(String key, Integer count);

	/**
	 * (1) count metric: accumulated value in a minute is calculated.
	 * @param key
	 */
	void countByOne(String key);


	/**
	 * (2) latency metric : mean,p50,p90,p95,p99,p100 latency values in a minute are calculated.
	 *	ex.
	 *		@Autowired
	 *		private MetricUpdater metricUpdater;
	 *
	 *		//...
	 *
	 *		Long initTime = System.currentTimeMillis();
	 *		// a logic whose latency is measured
	 *		metricUpdater.latency("yourLatencyMetricKey", initTime);
	 *
	 * Then, you can check the latency metric by set gradana query to 'hosts.($service).($role).($host).metric.yourLatencyMetricKey'
	 *
	 * @param key
	 * @param initTime
	 */
	void latency(String key, Long initTime);

	/**
	 * (2) latency metric : mean,p50,p90,p95,p99,p100 latency values in a minute are calculated.
	 * @param key
	 * @param initTime
	 * @param endTime
	 */
	void latency(String key, Long initTime, Long endTime);

	/**
	 * Sending MetricDto to MetricService
	 * @see com.jg.metricsdk.service.MetricService
	 * @param metric
	 */
	void sendMetric(MetricUnit metric);
}

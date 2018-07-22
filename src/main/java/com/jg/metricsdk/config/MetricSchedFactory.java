package com.jg.metricsdk.config;

import io.reactivex.Scheduler;
import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MetricSchedFactory {

	@Getter
	private Scheduler schedMetric;

	public MetricSchedFactory(Integer metricThreadMinSize, Integer metricThreadMaxSize, Integer metricThreadAliveTime) {
		if(metricThreadMinSize > metricThreadMaxSize ) {
			throw new IllegalArgumentException("Metric SDK: metricThreadMinSize (" + metricThreadMinSize +
				") must be less than metricThreadMaxSize (" + metricThreadMaxSize + ")");
		}

		schedMetric = Schedulers.from( make(metricThreadMinSize, metricThreadMaxSize, metricThreadAliveTime) );
	}

	private static ThreadPoolExecutor make(int min, int max, long aliveTime) {
		return new ThreadPoolExecutor(
			min,
			max,
			aliveTime,
			TimeUnit.MILLISECONDS,
			new SynchronousQueue<>(),
			new RxThreadFactory("sched-metric-%d"));
	}
}

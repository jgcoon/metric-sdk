package com.jg.metricsdk.rx;

import com.jg.metricsdk.Metric;
import com.jg.metricsdk.util.LongHolder;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class MetricObs {

	/**
	 * Metric Observable for Rx java version 2
	 * ex.
	 * 	io.reactivex.Observable.from(...).compose(MetricObsRxV1.count("metricKey"));
	 * @param key
	 * @param <T>
	 * @return
	 */
	//count
	public static <T> ObservableTransformer<T, T> countByOne(String key) {
		return observable -> processCountObs(observable, key, 1L);
	}

	public static <T> ObservableTransformer<T, T> count(String key, Integer value) {
		return observable -> processCountObs(observable, key, value.longValue());
	}

	public static <T> ObservableTransformer<T, T> count(String key, Long value) {
		return observable -> processCountObs(observable, key, value);
	}

	private static <T> Observable<T> processCountObs(Observable<T> observable, String key, Long value) {
		return observable.doOnNext( n -> Metric.key(key).count(value).send() );
	}

	//latency
	public static <T> ObservableTransformer<T, T> latency(String key) {
		LongHolder startTimeHolder = new LongHolder(0L);
		return observable -> observable
			.doOnSubscribe(s -> startTimeHolder.set(System.currentTimeMillis()))
			.doOnNext(n -> Metric.key(key).latency(startTimeHolder.get()).send() );
	}

	public static <T> ObservableTransformer<T, T> latency(String key, Long startTime) {
		return observable -> observable
			.doOnNext(n -> Metric.key(key).latency(startTime).send() );
	}

	public static <T> ObservableTransformer<T, T> latency(String key, Long startTime, Long endTime) {
		return observable -> observable
			.doOnNext(n -> Metric.key(key).latency(startTime, endTime).send() );
	}
}

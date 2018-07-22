package com.jg.metricsdk.service;

import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.MetricUnit;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LatencyMetricService extends com.jg.metricsdk.service.MetricServiceBase {

	private static PublishSubject<MetricUnit> publishSubject = PublishSubject.create();

	private static BiFunction<MetricUnit, MetricUnit, MetricUnit> reducer = (dto1, dto2) -> {
		dto2.getValue().forEach(val ->
			binaryAddition(dto1.getValue(), val, 0, dto1.getValue().size() - 1)
		);
		return dto1;
	};

	private static void binaryAddition(List<Long> list, long val, int start, int end) {
		int midIndex = (start + end) / 2;
		long midVal = list.get(midIndex);

		if (start == end) {
			if (midVal < val) {
				list.add(midIndex + 1, val);
			} else {
				list.add(midIndex, val);
			}
		} else if (midVal < val) {
			binaryAddition(list, val, midIndex + 1, end);
		} else {
			binaryAddition(list, val, start, midIndex);
		}
	}

	public LatencyMetricService(Consumer<MetricUnit> metricPublisher, MetricSchedFactory metricSchedFactory) {
		super(metricPublisher, metricSchedFactory, reducer, publishSubject);

		publishSubject
			.buffer(60, TimeUnit.SECONDS) // calculation unit
			.doOnError(e -> log.error(e.getMessage()))
			.subscribe(super::postAccept);
	}
}
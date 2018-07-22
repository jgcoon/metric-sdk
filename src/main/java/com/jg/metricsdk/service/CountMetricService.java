package com.jg.metricsdk.service;

import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.MetricUnit;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CountMetricService extends com.jg.metricsdk.service.MetricServiceBase {

	private static PublishSubject<MetricUnit> publishSubject = PublishSubject.create();

	private static BiFunction<MetricUnit, MetricUnit, MetricUnit> reducer = (dto1, dto2) -> {
		dto1.getValue().set(0, dto1.getValue().get(0) + dto2.getValue().get(0));
		return dto1;
	};

	public CountMetricService(Consumer<MetricUnit> metricPublisher, MetricSchedFactory metricSchedFactory) {
		super(metricPublisher, metricSchedFactory, reducer, publishSubject);

		publishSubject
			.buffer(60, TimeUnit.SECONDS) // calculation unit
			.doOnError(e -> log.error(e.getMessage()))
			.subscribe(super::postAccept);
	}
}

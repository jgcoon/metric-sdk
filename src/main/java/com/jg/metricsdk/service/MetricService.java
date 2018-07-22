package com.jg.metricsdk.service;

import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.MetricType;
import com.jg.metricsdk.model.MetricUnit;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class MetricService implements Consumer<MetricUnit> {

	private PublishSubject<MetricUnit> publishSubject = PublishSubject.create();

	private com.jg.metricsdk.service.CountMetricService countMetricService;
	private LatencyMetricService latencyMetricService;
	private MetricSchedFactory metricSchedFactory;

	public MetricService(CountMetricService countMetricService, LatencyMetricService latencyMetricService, MetricSchedFactory metricSchedFactory, Integer metricWindowTime) {

		this.countMetricService = countMetricService;
		this.latencyMetricService = latencyMetricService;
		this.metricSchedFactory = metricSchedFactory;

		this.publishSubject
			.buffer(metricWindowTime, TimeUnit.SECONDS) // window
			.doOnError(e -> log.error(e.getMessage()))
			.subscribe(process);
	}

	private Consumer<List<MetricUnit>> process = dtoList -> {
		Observable<GroupedObservable<MetricType, MetricUnit>> sharedObs =
			Observable.fromIterable(dtoList)
				.groupBy(MetricUnit::getType)
				.subscribeOn(metricSchedFactory.getSchedMetric())
				.share();

		sharedObs
			.filter(grouped -> MetricType.COUNT.equals(grouped.getKey()))
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.map(GroupedObservable::toList)
			.map(Single::toObservable)
			.subscribe(countMetricService::preAccept);

		sharedObs
			.filter(grouped -> MetricType.LATENCY.equals(grouped.getKey()))
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.map(GroupedObservable::toList)
			.map(Single::toObservable)
			.subscribe(latencyMetricService::preAccept);
	};

	private Predicate<MetricUnit> metricDtoNotNullPredicate = metric ->
		Objects.nonNull(metric)
			&& StringUtils.isNotEmpty(metric.getKey())
			&& Objects.nonNull(metric.getValue())
			&& Objects.nonNull(metric.getType());

	@Override
	public void accept(MetricUnit metric) {
		if(metricDtoNotNullPredicate.test(metric)) {
			publishSubject.onNext(metric);
		}
	}
}

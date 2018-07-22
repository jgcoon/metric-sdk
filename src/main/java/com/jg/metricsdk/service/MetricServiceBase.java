package com.jg.metricsdk.service;

import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.MetricUnit;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
class MetricServiceBase {
	private Consumer<MetricUnit> publisher;

	private MetricSchedFactory metricSchedFactory;

	private BiFunction<MetricUnit, MetricUnit, MetricUnit> reducer;
	private PublishSubject<MetricUnit> publishSubject;

	void preAccept(Observable<List<MetricUnit>> metricObs) {
		metricObs
			.flatMap(Observable::fromIterable)
			.groupBy(MetricUnit::getKey)
			.doOnError(e -> log.error(e.getMessage()))
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.subscribe(groupedObs ->
				groupedObs
					.reduce(reducer)
					.subscribeOn(metricSchedFactory.getSchedMetric())
					.subscribe(publishSubject::onNext)
			);
	}

	void postAccept(List<MetricUnit> dtoList) {
		Observable.fromIterable(dtoList)
			.groupBy(MetricUnit::getKey)
			.doOnError(e -> log.error(e.getMessage()))
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.subscribe(groupedObs -> groupedObs
				.reduce(reducer)
				.subscribeOn(metricSchedFactory.getSchedMetric())
				.subscribe(publisher)
			);
	}
}

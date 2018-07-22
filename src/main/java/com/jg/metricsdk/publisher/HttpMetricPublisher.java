package com.jg.metricsdk.publisher;

import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.HttpDto;
import com.jg.metricsdk.model.MetricType;
import com.jg.metricsdk.model.MetricUnit;
import com.jg.metricsdk.service.LatencyMetricHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jg.metricsdk.util.MetricDtoUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Slf4j
public class HttpMetricPublisher implements Consumer<MetricUnit> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private String httpHost;
	private String metricBasicPath;

	private MetricSchedFactory metricSchedFactory;
	private LatencyMetricHandler latencyMetricHandler;

	private static final String keyFormat = "%s.metric.%s";

	public HttpMetricPublisher(String httpHost, String metricBasicPath, MetricSchedFactory metricSchedFactory, LatencyMetricHandler latencyMetricHandler) {

		this.httpHost = httpHost;

		this.metricBasicPath = metricBasicPath;

		this.metricSchedFactory = metricSchedFactory;
		this.latencyMetricHandler = latencyMetricHandler;
	}

	@Override
	public void accept(MetricUnit metricUnit) {
		Observable.just(metricUnit)
			.map(unit -> {
				List<HttpDto> grafanaDtos = Lists.newArrayList();
				String fullKey = MetricDtoUtil.getFullKey(metricBasicPath, unit.getKey());

				if (unit.getType() == MetricType.COUNT) {
					grafanaDtos = Lists.newArrayList(new HttpDto(fullKey, unit.getValue().get(0), System.currentTimeMillis()));
				} else if (unit.getType() == MetricType.LATENCY) {
					grafanaDtos = latencyMetricHandler.convert(fullKey, unit);
				}

				return grafanaDtos;
			})
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.subscribe(this::httpSend);
	}

	public void httpSend (List<HttpDto> httpDtos) {
		Observable.fromIterable(httpDtos)
			.subscribeOn(metricSchedFactory.getSchedMetric())
			.subscribe(dto -> {
				URL url = new URL(httpHost);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json");
				con.setDoOutput(true);

				String jsonValue = objectMapper.writeValueAsString(dto);

				OutputStream os = con.getOutputStream();
				os.write(jsonValue.getBytes("UTF-8"));
				os.close();

				log.debug("{} {} {}", url, con.getResponseCode(), jsonValue);
			});
	}
}

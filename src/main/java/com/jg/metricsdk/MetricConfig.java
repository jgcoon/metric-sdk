package com.jg.metricsdk;

import com.jg.metricsdk.annotation.MetricAspect;
import com.jg.metricsdk.config.MetricSchedFactory;
import com.jg.metricsdk.model.MetricUnit;
import com.jg.metricsdk.model.TxMode;
import com.jg.metricsdk.publisher.HttpMetricPublisher;
import com.jg.metricsdk.publisher.KafkaMetricPublisher;
import com.jg.metricsdk.service.*;
import io.reactivex.functions.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.*;

@Data
@Configuration
@ComponentScan
@Slf4j
public class MetricConfig {

	/**
	 * @value Metric Properties
	 */
	private static final String METIC_BASIC_PATH = "metric.basic.path";						/** @code metric.basic.path		**/
	private static final String METRIC_TX_MODE = "metric.tx.mode";						/** @code metric.basic.mode		**/

	private static final String KAFKA_HOST = "metric.kafka.host";							/** @code metric.kafka.host 	**/
	private static final String KAFKA_TOPIC = "metric.kafka.topic";							/** @code metric.kafka.topic	**/

	private static final String HTTP_HOST = "metric.http.host";								/** @code metric.http.host		**/

	private static final String METRIC_LATENCY_PERCENTILE = "metric.latency.percentile";	/** @code metric.latency.percentile	**/
	private static final String METRIC_LATENCY_RANGE = "metric.latency.range";				/** @code metric.latency.range		**/

	private static final String METRIC_THREAD_MAX_SIZE = "metric.thread.max";				/** @code metric.thread.max			**/
	private static final String METRIC_THREAD_MIN_SIZE = "metric.thread.min";				/** @code metric.thread.min			**/
	private static final String METRIC_THREAD_ALIVE_TIME = "metric.thread.alivetime";		/** @code metric.thread.alivetime	**/

	private static final String METRIC_WINDOW_TIME = "metric.window.time";					/** @code metric.window.time		**/

	@Autowired
	private Environment environment;
	@Autowired
	private ApplicationContext applicationContext;

	@Bean(name="metricSdk_metricBasicPath")
	public String metricBasicPath() {
		String basicPath = getMetricProperty(METIC_BASIC_PATH);

		if (StringUtils.isNotEmpty(basicPath)) {
			throw new IllegalArgumentException("Metric SDK: basic metric path(key) is not defined. Please put value of 'metric.basic.path' in a property file");
		}

		return basicPath;
	}

	@Bean(name="metricSdk_metricTxMode")
	public TxMode metricSdk_httpTx() {
		String txMode = getMetricProperty(METRIC_TX_MODE);
		if(Objects.isNull(txMode)) {
			log.info("Metric SDK: 'metric.tx.mode' is null, metric is sent by http POST using JSON format");
			return TxMode.Http;
		}

		for(TxMode mode: TxMode.values()) {
			if(StringUtils.equalsAnyIgnoreCase(mode.toString(), txMode)) {
				return mode;
			}
		}
		log.info("Metric SDK: 'metric.tx.mode' is null, metric is sent by http POST using JSON format");
		return TxMode.Http;
	}

	@Bean(name="metricSdk_metricKafkaHost")
	public String metricKafkaHost(@Qualifier("metricSdk_metricTxMode") TxMode txMode) {
		String hostName = getMetricProperty(KAFKA_HOST);
		if(txMode == TxMode.Kafka && Objects.isNull(hostName)) {
			throw new IllegalArgumentException("Metric SDK: Kafka Tx mode. Please put value of 'metric.kafka.host' in a property file.");
		}
		return hostName;
	}

	@Bean(name="metricSdk_metricKafkaTopic")
	public String metricKafkaTopic(@Qualifier("metricSdk_metricTxMode") TxMode txMode) {
		String topicName = getMetricProperty(KAFKA_TOPIC);
		if(txMode == TxMode.Kafka && Objects.isNull(topicName)) {
			throw new IllegalArgumentException("Metric SDK: Kafka Tx mode. Please put value of 'metric.kafka.topic' in a property file.");
		}
		return topicName;
	}

	@Bean(name="metricSdk_metricHttpHost")
	public String grafanaHost(@Qualifier("metricSdk_metricTxMode") TxMode txMode) {
		String url = getMetricProperty(HTTP_HOST);

		if(txMode == TxMode.Http && Objects.isNull(url)) {
			throw new IllegalArgumentException("Metric SDK: Http Tx mode. Please put value of 'metric.http.host' in a property file.");
		}
		return url;
	}

	@Bean(name="metricSdk_metricThreadAliveTime")
	public Integer metricThreadAliveTime() {
		String threadAliveTime = getMetricProperty(METRIC_THREAD_ALIVE_TIME);
		if(Objects.isNull(threadAliveTime)) {
			log.info("Metric SDK: default thread alive time is set (default = 30,000 milliseconds)");
			return 30000;
		}
		return Integer.parseInt(threadAliveTime);
	}


	@Bean(name="metricSdk_metricThreadMinSize")
	public Integer metricThreadMinSize() {
		String threadMinSize = getMetricProperty(METRIC_THREAD_MIN_SIZE);
		if(Objects.isNull(threadMinSize)) {
			log.info("Metric SDK: default minimum thread pool size for metric processing is set (default = 10 threads)");
			return 10;
		}
		return Integer.parseInt(threadMinSize);
	}

	@Bean(name="metricSdk_metricThreadMaxSize")
	public Integer metricThreadMaxSize() {
		String threadMaxSize = getMetricProperty(METRIC_THREAD_MAX_SIZE);
		if(Objects.isNull(threadMaxSize)) {
			log.info("Metric SDK: default maximum thread pool size for metric processing is set (default = 20 threads)");
			return 20;
		}
		return Integer.parseInt(threadMaxSize);
	}

	@Bean(name="metricSdk_metricWindowTime")
	public Integer metricWindowTime() {
		String threadPoolSize = getMetricProperty(METRIC_WINDOW_TIME);
		if(Objects.isNull(threadPoolSize)) {
			log.info("Metric SDK: metric calculation window time is set to 10 sec");
			return 10;
		}

		Integer window = Integer.parseInt(threadPoolSize);
		if(60 % window != 0 || window < 0 || window > 60) {
			throw new IllegalArgumentException("Metric SDK: metric calculation window time must be a divisor of 60 and between 0 and 60 (e.g. 10, 20, 30, 60) ");
		}

		return window;
	}

	@Bean(name="metricSdk_metricSchedFactory")
	public MetricSchedFactory metricSchedFactory(
		@Qualifier("metricSdk_metricThreadMaxSize") Integer metricThreadMaxSize,
		@Qualifier("metricSdk_metricThreadMinSize") Integer metricThreadMinSize,
		@Qualifier("metricSdk_metricThreadAliveTime") Integer metricThreadAliveTime) {
		return new MetricSchedFactory(metricThreadMinSize, metricThreadMaxSize, metricThreadAliveTime);
	}

	@Bean(name="metricSdk_metricKafkaPublisher")
	public KafkaMetricPublisher metricKafkaPublisher(
		@Qualifier("metricSdk_metricKafkaHost") String metricKafkaHost,
		@Qualifier("metricSdk_metricKafkaTopic") String metricKafkaTopic,
		@Qualifier("metricSdk_metricBasicPath") String metricBasicPath,
		@Qualifier("metricSdk_metricSchedFactory") MetricSchedFactory metricSchedFactory) {
		return new KafkaMetricPublisher(metricKafkaHost, metricKafkaTopic, metricBasicPath, metricSchedFactory);
	}

	@Bean(name="metricSdk_latencyMetricHandler")
	public LatencyMetricHandler latencyMetricHandler() {
		return new LatencyMetricHandler(metricPercentile(), metricRange());
	}

	@Bean(name="metricSdk_metricPublisher")
	public Consumer<MetricUnit> metricPublisher(
		@Qualifier("metricSdk_metricTxMode") TxMode txMode,
		@Qualifier("metricSdk_metricKafkaPublisher") KafkaMetricPublisher metricKafkaPublisher,
		@Qualifier("metricSdk_metricHttpHost") String metricHttpHost,
		@Qualifier("metricSdk_metricSchedFactory") MetricSchedFactory metricSchedFactory,
		@Qualifier("metricSdk_metricBasicPath") String metricBasicPath,
		@Qualifier("metricSdk_latencyMetricHandler") LatencyMetricHandler latencyMetricHandler ) {
		if(txMode == TxMode.Http) {
			return new HttpMetricPublisher(metricHttpHost, metricBasicPath, metricSchedFactory, latencyMetricHandler);
		} else {
			return metricKafkaPublisher;
		}
	}

	@Bean(name="metricSdk_countMetricService")
	public CountMetricService countMetricService(
		@Qualifier("metricSdk_metricPublisher") Consumer<MetricUnit> metricPublisher,
		@Qualifier("metricSdk_metricSchedFactory") MetricSchedFactory metricSchedFactory) {
		return new CountMetricService(metricPublisher, metricSchedFactory);
	}

	@Bean(name="metricSdk_latencyMetricService")
	public LatencyMetricService latencyMetricService(
		@Qualifier("metricSdk_metricPublisher") Consumer<MetricUnit> metricPublisher,
		@Qualifier("metricSdk_metricBasicPath") String metricBasicPath,
		@Qualifier("metricSdk_metricSchedFactory") MetricSchedFactory metricSchedFactory) {

		log.info("Metric SDK: basic metric path = " + metricBasicPath);
		return new LatencyMetricService(metricPublisher, metricSchedFactory);
	}


	@Bean(name="metricSdk_metricService")
	public MetricService metricService(
		@Qualifier("metricSdk_countMetricService") CountMetricService countMetricService,
		@Qualifier("metricSdk_latencyMetricService") LatencyMetricService latencyMetricService,
		@Qualifier("metricSdk_metricSchedFactory") MetricSchedFactory metricSchedFactory,
		@Qualifier("metricSdk_metricWindowTime") Integer metricWindowTime ) {
		return new MetricService(countMetricService, latencyMetricService, metricSchedFactory, metricWindowTime);
	}

	@Bean(name="metricSdk_metricUpdater")
	public MetricUpdater metricUpdater(
		@Qualifier("metricSdk_metricService") MetricService metricService ){
		return new com.jg.metricsdk.MetricUpdaterImpl(metricService);
	}

	@Bean(name="metricSdk_Metric")
	public Metric metricSdk_Metric() { return new Metric(); }

	@Bean(name="metricSdk_metricAspect")
	public MetricAspect metricAspect(
		@Qualifier("metricSdk_metricUpdater") MetricUpdater metricUpdater ){
		return new MetricAspect(metricUpdater);
	}

	public List<Integer> metricPercentile() {
		List<Integer> defaultPercentile = Lists.newArrayList(100, 99, 95, 90, 50);
		String percentileStr = getMetricProperty(METRIC_LATENCY_PERCENTILE);

		if(StringUtils.isEmpty(percentileStr)) {
			log.info("Metric SDK: default percentile values are set (100, 99, 95, 90, 50)");
			return defaultPercentile;
		}

		List<Integer> percentileList = Lists.newArrayList();
		try {
			StringTokenizer st = new StringTokenizer(percentileStr, ",");

			while (st.hasMoreElements()) {
				Integer percentValue = Integer.parseInt(st.nextToken().trim());
				if (percentValue > 100 || percentValue < 0) {
					throw new IllegalArgumentException("Metric SDK: percentile value must be defined in the range of 0 ~ 100");
				} else {
					percentileList.add(percentValue);
				}
			}

			percentileList.sort((o1, o2) -> o2.compareTo(o1)); //역순으로 정렬해야함
			return percentileList;
		} catch (Exception e) {
			log.info("Metric SDK: percentile values are wrongly defined, (e.x. metric.latency.percentile = 1,5,10,50,90,95,100)");
			log.info("Metric SDK: default percentile values are set (100, 99, 95, 90, 50)");
			return defaultPercentile;
		}
	}

	public List<Integer> metricRange() {
		List<Integer> defaultRange = Lists.newArrayList(50, 100, 150, 200, 250, 300);
		String rangeStr = getMetricProperty(METRIC_LATENCY_RANGE);

		if(StringUtils.isEmpty(rangeStr)) {
			log.info("Metric SDK: default range values are set (50, 100, 150, 200, 250, 300)");
			return defaultRange;
		}

		List<Integer> rangeList = Lists.newArrayList();
		try {
			StringTokenizer st = new StringTokenizer(rangeStr, ",");
			while (st.hasMoreElements()) {
				rangeList.add( Integer.parseInt(st.nextToken().trim()) );
			}

			rangeList.sort((o1, o2) -> o1.compareTo(o2)); //순서대로 정렬해야함
			return rangeList;
		} catch (Exception e) {
			log.info("Metric SDK: range values are wrongly defined, (e.x. metric.latency.percentile = 1,5,10,50,90,95,100)");
			log.info("Metric SDK: default range values are set (50, 100, 150, 200, 250, 300)");
			return defaultRange;
		}
	}

	private String getMetricProperty( String propertyKey ) {
		for (Iterator it = ((AbstractEnvironment) environment).getPropertySources().iterator(); it.hasNext(); ) {
			PropertySource propertySource = (PropertySource) it.next();
			if (propertySource instanceof MapPropertySource) {
				//Find propertyKey for setting environment first (local, prod, it)
				if(!Arrays.isNullOrEmpty(environment.getActiveProfiles())) {
					for (String profile : environment.getActiveProfiles()) {
						if (propertySource.getName().contains(profile)) {
							for (Map.Entry<String, Object> entry : ((MapPropertySource) propertySource).getSource().entrySet()) {
								if (StringUtils.equals(propertyKey, entry.getKey())) {
									return String.valueOf(entry.getValue());
								}
							}
						}
					}
				}
				//Find propertyKey for all property files
				for (Map.Entry<String, Object> entry : ((MapPropertySource) propertySource).getSource().entrySet()) {
					if (StringUtils.equals(propertyKey, entry.getKey())) {
						return String.valueOf(entry.getValue());
					}
				}
			}
		}

		return null;
	}
}

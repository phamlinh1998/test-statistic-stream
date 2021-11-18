package com.example.testeventlog.streams;

import com.example.testeventlog.config.TopicConfig;
import com.example.testeventlog.utils.DateRangeUtils;
import com.example.testeventlog.utils.KeyUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiinvent.event_log.EventLog;

import java.time.LocalDate;
import java.util.Objects;

@Configuration
@Log4j2
public class EventLogStreams {

  @Autowired Stores stores;

  @Bean
  public KStream<String, EventLog> eventLogStream(StreamsBuilder streamsBuilder) {
    KStream<String, EventLog> eventLogStream = streamsBuilder.stream(TopicConfig.EVENT_LOG_TOPIC);
    log.info("=========== true ===============");
    return eventLogStream
        .peek((key, event) -> log.info("NEW DOMAIN EVENT: {} -----> {}", key, event))
        .filter((key, event) -> Objects.nonNull(event));
  }

  @Bean
  public KStream<Long, EventLog> eventByCampaignStream(KStream<String, EventLog> eventLogStream) {
    return eventLogStream.selectKey((key, event) -> event.getCampaignId());
  }

  @Bean
  public KStream<String, EventLog> eventByFrequencyStream(
      KStream<String, EventLog> eventLogStream) {

    return eventLogStream.selectKey(
        (key, event) -> {
          String firstViewKey = KeyUtils.firstViewKey(event.getDeviceId(), event.getCampaignId());
          Long firstViewTime = stores.campaignFirstViewGlobalStore().get(firstViewKey);
          log.info("========== first view time {}", firstViewTime);
          firstViewTime = ObjectUtils.defaultIfNull(firstViewTime, event.getServerTime());

          Pair<LocalDate, LocalDate> range =
              DateRangeUtils.getCurrentRange(
                  firstViewTime, System.currentTimeMillis(), event.getCampaignFrequency());

          String frequencyKey =
              KeyUtils.frequencyKey(
                  event.getDeviceId(), event.getCampaignId(), range.getLeft(), range.getRight());

          log.info("============ frequency {}", frequencyKey);
          return frequencyKey;
        });
  }
}

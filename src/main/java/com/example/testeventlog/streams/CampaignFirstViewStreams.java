package com.example.testeventlog.streams;

import com.example.testeventlog.config.TopicConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiinvent.event_log.EventLog;

import java.util.Objects;

@Configuration
@Log4j2
public class CampaignFirstViewStreams {

  @Bean
  public KStream<String, Long> campaignFirstViewKStream(KStream<String, EventLog> eventLogStream) {

    // TODO (thuync): use common lang for checking multiple objects for nullity

    KStream<String, Long> campaignFirstViewStream =
        eventLogStream
            .filter(
                (eventId, eventLog) ->
                    Objects.nonNull(eventLog.getDeviceId())
                        && Objects.nonNull(eventLog.getCampaignId()))
            .selectKey( // re-key to {deviceId}_{campaignId}
                (eventId, eventLog) ->
                    String.format("%s_%s", eventLog.getDeviceId(), eventLog.getCampaignId()))
            .groupByKey(Serialized.with(Serdes.String(), null))
            .aggregate(
                () -> -1L,
                (key, eventLog, startTime) -> startTime < 0 ? eventLog.getServerTime() : startTime,
                Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                        Stores.CAMPAIGN_FIRST_VIEW_STORE)
                    .withValueSerde(Serdes.Long()))
            .toStream()
            .peek((key, value) -> log.info("value {}", value));

    campaignFirstViewStream.to(
        TopicConfig.CAMPAIGN_FIRST_VIEW_TOPIC, Produced.valueSerde(Serdes.Long()));
    return campaignFirstViewStream;
  }

  @Bean
  public GlobalKTable<String, Long> campaignFirstViewTable(StreamsBuilder streamsBuilder) {

    // map {deviceId}_{campaignId} --> first view's start time
    return streamsBuilder.globalTable(
        TopicConfig.CAMPAIGN_FIRST_VIEW_TOPIC,
        Consumed.with(Serdes.String(), Serdes.Long()),
        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
            Stores.CAMPAIGN_FIRST_VIEW_GLOBAL_STORE));
  }
}

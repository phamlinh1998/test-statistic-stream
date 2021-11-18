package com.example.testeventlog.streams;

import com.example.testeventlog.config.TopicConfig;
import com.example.testeventlog.utils.KeyUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiinvent.campaign.CampaignViewFrequency;
import wiinvent.event_log.EventLog;
import wiinvent.stats.CampaignStatistics;
import wiinvent.stats.Statistics;

import java.util.Map;

@Configuration
@Log4j2
public class CampaignStatisticsStreams {
  public static String CAMPAIGN_STATISTICS_STORE = "wiinvent.campaign.statistics.store";
  public static String CAMPAIGN_FREQUENCY_STORE = "wiinvent.campaign.frequency.store";

  private static void increaseClick(Statistics statistics) {
    statistics.setClick(statistics.getClick() + 1);
  }

  private static void increaseImpression(Statistics statistics) {
    statistics.setImpression(statistics.getImpression() + 1);
  }

  private static CampaignStatistics initCampaignStatistics() {
    return CampaignStatistics.newBuilder().setTotal(Statistics.newBuilder().build()).build();
  }

  @Bean
  public KTable<Long, CampaignStatistics> campaignStatisticsStream(
      KStream<Long, EventLog> eventByCampaignStream) {

    KTable<Long, CampaignStatistics> campaignStatisticsTable =
        eventByCampaignStream
            .groupByKey(Grouped.with(Serdes.Long(), null)) // null for default Serde
            .aggregate(
                CampaignStatisticsStreams::initCampaignStatistics,
                (campaignId, event, campaignStatistics) -> {
                  campaignStatistics.setCampaignId(campaignId);

                  Statistics totalStatistics = campaignStatistics.getTotal();
                  Map<String, Statistics> platforms = campaignStatistics.getPlatforms();
                  Statistics platformStatistics =
                      platforms.getOrDefault(event.getPlatform(), Statistics.newBuilder().build());

                  switch (event.getType()) {
                    case "CLICK":
                      increaseClick(totalStatistics);
                      increaseClick(platformStatistics);
                      break;
                    case "IMPRESSION":
                      increaseImpression(totalStatistics);
                      increaseImpression(platformStatistics);
                      break;
                    default:
                      break;
                  }

                  platforms.put(event.getPlatform(), platformStatistics);
                  return campaignStatistics;
                },
                Materialized.<Long, CampaignStatistics, KeyValueStore<Bytes, byte[]>>as(
                        CAMPAIGN_STATISTICS_STORE)
                    .withKeySerde(Serdes.Long()));

    campaignStatisticsTable
        .toStream()
        .to(TopicConfig.CAMPAIGN_STATISTICAL_TOPIC, Produced.keySerde(Serdes.Long()));

    return campaignStatisticsTable;
  }

  @Bean
  public KStream<String, CampaignViewFrequency> viewFrequencyStream(
      KStream<String, EventLog> eventByFrequencyStream) {

    KTable<String, CampaignViewFrequency> viewFrequencyTable =
        eventByFrequencyStream
            .groupByKey(Grouped.with(Serdes.String(), null))
            .aggregate(
                CampaignViewFrequency.newBuilder()::build,
                (key, event, viewFrequency) -> {
                  viewFrequency.setCampaignId(event.getCampaignId());
                  viewFrequency.setDeviceId(event.getDeviceId());

                  String[] parts = key.split(KeyUtils.DELIMITER);
                  String startDate = parts[2];
                  String endDate = parts[3];

                  viewFrequency.setId(key);
                  viewFrequency.setStartDate(startDate);
                  viewFrequency.setEndDate(endDate);

                  viewFrequency.setCampaignFrequency(String.valueOf(event.getCampaignFrequency()));
                  viewFrequency.setCount(viewFrequency.getCount() + 1);

                  log.info("========= view frequency table {}", viewFrequency);

                  return viewFrequency;
                },
                Materialized.<String, CampaignViewFrequency, KeyValueStore<Bytes, byte[]>>as(
                        CAMPAIGN_FREQUENCY_STORE)
                    .withKeySerde(Serdes.String()));

    KStream<String, CampaignViewFrequency> viewFrequencyStream = viewFrequencyTable.toStream();
    viewFrequencyStream.to(
        TopicConfig.CAMPAIGN_VIEW_FREQUENCY_TOPIC, Produced.keySerde(Serdes.String()));

    return viewFrequencyStream;
  }
}

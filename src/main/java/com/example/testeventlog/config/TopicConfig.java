package com.example.testeventlog.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
  public static final String EVENT_LOG_TOPIC = "linh.test.event_log";
  public static final String CAMPAIGN_STATISTICAL_TOPIC = "linh.test.campaign.statistical";
  public static final String CAMPAIGN_FIRST_VIEW_TOPIC = "linh.test.campaign.first.view";
  public static final String CAMPAIGN_VIEW_FREQUENCY_TOPIC = "linh.test.campaign.view.frequency";

  @Value("${spring.kafka.topic.num-partitions}")
  private int numPartitions;

  @Value("${spring.kafka.topic.replication-factor}")
  private short replicationFactor;

  @Bean
  public NewTopic campaignStatisticalTopic() {
    return new NewTopic(CAMPAIGN_STATISTICAL_TOPIC, numPartitions, replicationFactor);
  }

  @Bean
  public NewTopic campaignFirstViewTopic() {
    return new NewTopic(CAMPAIGN_FIRST_VIEW_TOPIC, numPartitions, replicationFactor);
  }

  @Bean
  public NewTopic campaignFrequencyTopic() {
    return new NewTopic(CAMPAIGN_VIEW_FREQUENCY_TOPIC, numPartitions, replicationFactor);
  }
}

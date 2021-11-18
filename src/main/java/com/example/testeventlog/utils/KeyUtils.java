package com.example.testeventlog.utils;

import java.time.LocalDate;

public class KeyUtils {

  public static final String DELIMITER = "_";

  public static String firstViewKey(String deviceId, long campaignId) {
    return String.format("%s_%s", deviceId, campaignId);
  }

  public static String frequencyKey(
      String deviceId, long campaignId, LocalDate startDate, LocalDate endDate) {

    return String.format("%s_%s_%s_%s", deviceId, campaignId, startDate, endDate);
  }
}

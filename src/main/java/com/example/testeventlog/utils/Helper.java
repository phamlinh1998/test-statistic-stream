package com.example.testeventlog.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.Map;

public class Helper {

  public static Map<String, Object> objectToMap(Object object) {
    ObjectMapper oMapper = new ObjectMapper();
    return oMapper.convertValue(object, Map.class);
  }

  public static LocalDate getTodayDateHCMZone() {
    return getTodayDateTimeHCMZone().toLocalDate();
  }

  public static LocalDateTime getTodayDateTimeHCMZone() {
    return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
  }

  public static LocalDateTime convertFromHCMToUTC(LocalDateTime dateTimeHCM) {
    return dateTimeHCM
        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        .withZoneSameInstant(ZoneId.of("UTC"))
        .toLocalDateTime();
  }

  public static LocalDateTime convertToHCMDateTime(String dateTimeStr) {
    java.time.format.DateTimeFormatter formatter =
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr, formatter);
    ZoneId hcmZone = ZoneId.of("Asia/Ho_Chi_Minh");
    ZonedDateTime hcmZoned = zonedDateTime.withZoneSameInstant(hcmZone);
    return hcmZoned.toLocalDateTime();
  }

  public static Long getStartOfDayTimestamp(LocalDate localDate) {
    return localDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
  }

  public static Long getEndOfDayTimestamp(LocalDate localDate) {
    return localDate.atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
  }
}

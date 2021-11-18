package com.example.testeventlog.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DateRangeUtils {

  private static LocalDate toLocalDate(long timestamp, ZoneId zoneId) {
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public static Pair<LocalDate, LocalDate> getCurrentRange(
      long firstTime, long currentTime, String rangeString) {

    int rangeLength = getRangeLength(rangeString);
    return getCurrentRange(firstTime, currentTime, rangeLength);
  }

  public static Pair<LocalDate, LocalDate> getCurrentRange(
      long firstTime, long currentTime, int rangeLength) {

    // Todo(thuync): use Asia/Ho_Chi_Minh
    LocalDate firstDate = toLocalDate(firstTime, ZoneId.systemDefault());
    LocalDate currentDate = toLocalDate(currentTime, ZoneId.systemDefault());

    long days = ChronoUnit.DAYS.between(firstDate, currentDate);

    int rangeOrder = (int) days / rangeLength;

    LocalDate startRangeDate = firstDate.plusDays(rangeLength * rangeOrder);
    LocalDate endRangeDate = startRangeDate.plusDays(rangeLength - 1);

    return Pair.of(startRangeDate, endRangeDate);
  }

  public static int getRangeLength(String rangeString) {
    switch (rangeString) {
      case "WEEK":
        return 7;
      case "MONTH":
        return 30;
      default:
        return 1; // DAY
    }
  }
}

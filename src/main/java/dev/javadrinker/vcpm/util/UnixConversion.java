package dev.javadrinker.vcpm.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UnixConversion {
    public static long unixTimestampToMillis(String unixTimestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(unixTimestamp, formatter);

        return dateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }
}

package com.wplab.studyroom.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static String today() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}

	public static int parseHour(String value, int defaultHour) {
		try {
			int hour = Integer.parseInt(value);
			if (hour < 0 || hour > 23) {
				return defaultHour;
			}
			return hour;
		} catch (Exception e) {
			return defaultHour;
		}
	}

	public static Timestamp toTimestamp(String date, int hour) throws StudyroomException {
		try {
			String value = date + " " + two(hour) + ":00:00";
			return Timestamp.valueOf(value);
		} catch (Exception e) {
			throw new StudyroomException("날짜와 시간을 올바르게 입력하여야 합니다.", e);
		}
	}

	public static String two(int value) {
		return value < 10 ? "0" + value : String.valueOf(value);
	}
}

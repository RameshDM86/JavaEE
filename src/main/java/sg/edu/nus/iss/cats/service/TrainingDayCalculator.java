package sg.edu.nus.iss.cats.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public final class TrainingDayCalculator {

	private TrainingDayCalculator() {
	}

	public static boolean isWorkingDay(LocalDate date, Set<LocalDate> holidays) {
		DayOfWeek day = date.getDayOfWeek();
		if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
			return false;
		}
		return !holidays.contains(date);
	}

	public static BigDecimal countFullDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
		BigDecimal days = BigDecimal.ZERO;
		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
			if (isWorkingDay(date, holidays)) {
				days = days.add(BigDecimal.ONE);
			}
		}
		return days;
	}
}

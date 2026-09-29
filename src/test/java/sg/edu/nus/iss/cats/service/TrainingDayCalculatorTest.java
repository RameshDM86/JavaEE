package sg.edu.nus.iss.cats.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TrainingDayCalculatorTest {

	private static final Set<LocalDate> HOLIDAYS = Set.of(LocalDate.of(2026, 5, 1));

	@Test
	void weekendsAndPublicHolidaysAreNotWorkingDays() {
		assertFalse(TrainingDayCalculator.isWorkingDay(LocalDate.of(2026, 10, 10), HOLIDAYS));
		assertFalse(TrainingDayCalculator.isWorkingDay(LocalDate.of(2026, 10, 11), HOLIDAYS));
		assertFalse(TrainingDayCalculator.isWorkingDay(LocalDate.of(2026, 5, 1), HOLIDAYS));
		assertTrue(TrainingDayCalculator.isWorkingDay(LocalDate.of(2026, 10, 5), HOLIDAYS));
	}

	@Test
	void fullDayCountSkipsWeekendsAndHolidays() {
		BigDecimal week = TrainingDayCalculator.countFullDays(
				LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), HOLIDAYS);
		assertEquals(new BigDecimal("5"), week);

		BigDecimal withHoliday = TrainingDayCalculator.countFullDays(
				LocalDate.of(2026, 4, 27), LocalDate.of(2026, 5, 1), HOLIDAYS);
		assertEquals(new BigDecimal("4"), withHoliday);
	}
}

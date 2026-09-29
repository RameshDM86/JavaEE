package sg.edu.nus.iss.cats.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.cats.entity.CourseApplication;
import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.ApplicationStatus;
import sg.edu.nus.iss.cats.model.CourseCategory;
import sg.edu.nus.iss.cats.model.Designation;
import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.model.SessionType;
import sg.edu.nus.iss.cats.repository.EmployeeRepository;
import sg.edu.nus.iss.cats.repository.PublicHolidayRepository;
import sg.edu.nus.iss.cats.web.CourseApplicationForm;

@SpringBootTest
@Transactional
class CourseApplicationServiceTest {

	@Autowired
	private CourseApplicationService service;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private PublicHolidayRepository publicHolidayRepository;

	private Employee manager;
	private Employee employee;
	private Set<LocalDate> holidays;

	@BeforeEach
	void setUp() {
		manager = employeeRepository.findByUserId("jtan").orElseThrow();
		employee = new Employee("sam", "employee123", "Sam Lee", "sam.lee@iss.edu.sg", Role.EMPLOYEE);
		employee.setDesignation(Designation.ADMINISTRATIVE);
		employee.setTrainingDayEntitlement(new BigDecimal("5.0"));
		employee.setTrainingBudget(new BigDecimal("2000.00"));
		employee.setManager(manager);
		employee = employeeRepository.save(employee);
		holidays = publicHolidayRepository.findAll().stream()
				.map(holiday -> holiday.getHolidayDate())
				.collect(java.util.stream.Collectors.toSet());
	}

	@Test
	void submitsAFutureInternalHalfDay() {
		LocalDate day = nextWorkingDay(LocalDate.now().plusDays(21));
		CourseApplication saved = service.submit(employee.getId(), form(
				"Briefing skills", "INTERNAL_TRAINING", "ISS", day, day, "HALF_DAY", "", "Needed for stand-ups."));
		assertEquals(ApplicationStatus.APPLIED, saved.getStatus());
		assertEquals(new BigDecimal("0.5"), service.trainingDays(saved));
	}

	@Test
	void rejectsOverlapPastDatesWeekendsBudgetAndEntitlement() {
		LocalDate start = nextWorkingDay(futureBase());
		LocalDate end = endAfterWorkingDays(start, 3);
		service.submit(employee.getId(), form(
				"External workshop", "EXTERNAL_COURSE", "SkillsFuture", start, end, "FULL_DAY", "1500", "Role needs this."));

		assertError(form(
				"Clash", "EXTERNAL_COURSE", "SkillsFuture", start, end, "FULL_DAY", "100", "Same dates."),
				"overlaps");
		LocalDate past = LocalDate.of(2026, 1, 5);
		assertError(form(
				"Old", "INTERNAL_TRAINING", "ISS", past, past.plusDays(1), "FULL_DAY", "0", "Already happened."),
				"future date");
		LocalDate saturday = start;
		while (saturday.getDayOfWeek() != java.time.DayOfWeek.SATURDAY) {
			saturday = saturday.plusDays(1);
		}
		assertError(form(
				"Weekend", "INTERNAL_TRAINING", "ISS", saturday, saturday.plusDays(2), "FULL_DAY", "0", "Bad dates."),
				"working day");

		LocalDate feeDay = nextWorkingDay(end.plusDays(1));
		assertError(form(
				"Too expensive", "EXTERNAL_COURSE", "Vendor", feeDay, feeDay, "FULL_DAY", "600", "Over budget."),
				"budget");

		LocalDate laterStart = nextWorkingDay(end.plusDays(1));
		LocalDate laterEnd = endAfterWorkingDays(laterStart, 3);
		assertError(form(
				"Too many days", "INTERNAL_TRAINING", "ISS", laterStart, laterEnd, "FULL_DAY", "0", "Over entitlement."),
				"entitlement");
	}

	@Test
	void managerDecisionAndEmployeeFollowUp() {
		LocalDate day = nextWorkingDay(LocalDate.now().plusDays(50));
		CourseApplication saved = service.submit(employee.getId(), form(
				"Vendor course", "EXTERNAL_COURSE", "Vendor", day, day, "FULL_DAY", "400", "Supports the service desk."));

		assertErrorDecision(saved.getId(), "   ");
		service.decide(manager.getId(), saved.getId(), true, "Share a briefing with the team after the course.");
		assertEquals(ApplicationStatus.APPROVED, reload(saved.getId()).getStatus());

		service.cancel(employee.getId(), saved.getId());
		assertEquals(ApplicationStatus.CANCELLED, reload(saved.getId()).getStatus());
	}

	@Test
	void updateDeleteAndCompleteFollowTheStatusRules() {
		LocalDate day = nextWorkingDay(futureBase().plusDays(40));
		CourseApplication saved = service.submit(employee.getId(), form(
				"Draft title", "INTERNAL_TRAINING", "ISS", day, day, "FULL_DAY", "0", "First justification."));
		CourseApplicationForm edited = CourseApplicationForm.from(saved);
		edited.setTitle("Revised title");
		CourseApplication updated = service.update(employee.getId(), saved.getId(), edited);
		assertEquals(ApplicationStatus.UPDATED, updated.getStatus());
		assertEquals("Revised title", updated.getTitle());

		service.delete(employee.getId(), saved.getId());
		assertEquals(ApplicationStatus.DELETED, reload(saved.getId()).getStatus());

		CourseApplication past = service.submit(employee.getId(), form(
				"Will be backdated", "INTERNAL_TRAINING", "ISS", day, day, "FULL_DAY", "0", "Temporary."));
		past.setStartDate(LocalDate.now().minusDays(2));
		past.setEndDate(LocalDate.now().minusDays(1));
		past.setStatus(ApplicationStatus.APPROVED);
		courseApplicationRepository.save(past);

		service.complete(employee.getId(), past.getId(), "The handover notes were useful.");
		assertEquals(ApplicationStatus.COMPLETED, reload(past.getId()).getStatus());
	}

	@Autowired
	private sg.edu.nus.iss.cats.repository.CourseApplicationRepository courseApplicationRepository;

	private CourseApplication reload(Long id) {
		return courseApplicationRepository.findById(id).orElseThrow();
	}

	private void assertError(CourseApplicationForm form, String fragment) {
		RuleViolationException ex = assertThrows(RuleViolationException.class,
				() -> service.submit(employee.getId(), form));
		assertTrue(ex.getErrors().stream().anyMatch(error -> error.toLowerCase().contains(fragment)),
				ex.getErrors().toString());
	}

	private void assertErrorDecision(Long id, String comment) {
		RuleViolationException ex = assertThrows(RuleViolationException.class,
				() -> service.decide(manager.getId(), id, false, comment));
		assertTrue(ex.getErrors().stream().anyMatch(error -> error.toLowerCase().contains("reason")));
	}

	private CourseApplicationForm form(String title, String category, String provider, LocalDate start, LocalDate end,
			String session, String fee, String justification) {
		CourseApplicationForm form = new CourseApplicationForm();
		form.setTitle(title);
		form.setCategory(category);
		form.setProvider(provider);
		form.setStartDate(start.toString());
		form.setEndDate(end.toString());
		form.setSessionType(session);
		form.setFee(fee);
		form.setJustification(justification);
		form.setDissemination("Covered by the team.");
		return form;
	}

	private LocalDate futureBase() {
		LocalDate base = LocalDate.now().plusDays(21);
		if (base.getMonthValue() >= 11) {
			return LocalDate.of(base.getYear() + 1, 3, 2);
		}
		return base;
	}

	private LocalDate nextWorkingDay(LocalDate date) {
		LocalDate cursor = date;
		while (!TrainingDayCalculator.isWorkingDay(cursor, holidays)) {
			cursor = cursor.plusDays(1);
		}
		return cursor;
	}

	private LocalDate endAfterWorkingDays(LocalDate start, int workingDays) {
		LocalDate cursor = nextWorkingDay(start);
		int counted = 0;
		LocalDate end = cursor;
		while (counted < workingDays) {
			if (TrainingDayCalculator.isWorkingDay(cursor, holidays)) {
				counted++;
				end = cursor;
			}
			if (counted < workingDays) {
				cursor = cursor.plusDays(1);
			}
		}
		return end;
	}
}

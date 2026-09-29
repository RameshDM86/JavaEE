package sg.edu.nus.iss.cats.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.cats.entity.CourseApplication;
import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.entity.PublicHoliday;
import sg.edu.nus.iss.cats.model.ApplicationStatus;
import sg.edu.nus.iss.cats.model.CourseCategory;
import sg.edu.nus.iss.cats.model.Designation;
import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.model.SessionType;
import sg.edu.nus.iss.cats.repository.CourseApplicationRepository;
import sg.edu.nus.iss.cats.repository.EmployeeRepository;
import sg.edu.nus.iss.cats.repository.PublicHolidayRepository;

@Component
public class DataSeeder implements CommandLineRunner {

	private static final BigDecimal ANNUAL_BUDGET = new BigDecimal("2000.00");

	private final EmployeeRepository employeeRepository;
	private final CourseApplicationRepository courseApplicationRepository;
	private final PublicHolidayRepository publicHolidayRepository;

	public DataSeeder(EmployeeRepository employeeRepository,
			CourseApplicationRepository courseApplicationRepository,
			PublicHolidayRepository publicHolidayRepository) {
		this.employeeRepository = employeeRepository;
		this.courseApplicationRepository = courseApplicationRepository;
		this.publicHolidayRepository = publicHolidayRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (employeeRepository.count() > 0) {
			return;
		}

		Employee admin = employee("admin", "admin123", "Aisha Rahman", "aisha.rahman@iss.edu.sg", Role.ADMIN);
		Employee manager = employee("jtan", "manager123", "Jason Tan", "jason.tan@iss.edu.sg", Role.MANAGER);
		manager.setDesignation(Designation.PROFESSIONAL);
		manager.setTrainingDayEntitlement(Designation.PROFESSIONAL.getDefaultTrainingDays());
		manager.setTrainingBudget(ANNUAL_BUDGET);

		employeeRepository.save(admin);
		employeeRepository.save(manager);

		Employee priya = staff("pnair", "Priya Nair", "priya.nair@iss.edu.sg", Designation.ADMINISTRATIVE, manager);
		Employee wei = staff("wlim", "Wei Lim", "wei.lim@iss.edu.sg", Designation.PROFESSIONAL, manager);
		employeeRepository.save(priya);
		employeeRepository.save(wei);

		CourseApplication applied = new CourseApplication(
				wei,
				"Secure Coding Workshop",
				CourseCategory.INTERNAL_TRAINING,
				"ISS",
				LocalDate.of(2026, 10, 5),
				LocalDate.of(2026, 10, 6),
				SessionType.FULL_DAY,
				BigDecimal.ZERO,
				"The workshop covers input validation and access control used in our web modules.",
				"Team briefing in the next sprint review.",
				ApplicationStatus.APPLIED,
				LocalDateTime.of(2026, 9, 20, 9, 30));
		courseApplicationRepository.save(applied);

		CourseApplication approved = new CourseApplication(
				priya,
				"ITIL Foundation",
				CourseCategory.PROFESSIONAL_CERTIFICATION,
				"Axelos",
				LocalDate.of(2026, 11, 16),
				LocalDate.of(2026, 11, 18),
				SessionType.FULL_DAY,
				new BigDecimal("800.00"),
				"The certification supports the service desk process Priya owns.",
				"Admin team will cover the front desk on those three days.",
				ApplicationStatus.APPROVED,
				LocalDateTime.of(2026, 9, 18, 14, 0));
		approved.setManagerComment("Approved on the condition that Priya shares a short briefing with the admin team.");
		approved.setDecisionAt(LocalDateTime.of(2026, 9, 19, 11, 15));
		courseApplicationRepository.save(approved);

		CourseApplication ended = new CourseApplication(
				wei,
				"Workplace Communication",
				CourseCategory.INTERNAL_TRAINING,
				"ISS",
				LocalDate.of(2026, 8, 17),
				LocalDate.of(2026, 8, 18),
				SessionType.FULL_DAY,
				BigDecimal.ZERO,
				"The course covers briefing and handover skills used with project teams.",
				"Colleagues covered the stand-up notes on those two days.",
				ApplicationStatus.APPROVED,
				LocalDateTime.of(2026, 8, 1, 10, 0));
		ended.setManagerComment("Approved. Share the handover template with the team afterwards.");
		ended.setDecisionAt(LocalDateTime.of(2026, 8, 2, 9, 0));
		courseApplicationRepository.save(ended);

		seedHolidays();
	}

	private Employee employee(String userId, String password, String name, String email, Role role) {
		return new Employee(userId, password, name, email, role);
	}

	private Employee staff(String userId, String name, String email, Designation designation, Employee manager) {
		Employee employee = new Employee(userId, "employee123", name, email, Role.EMPLOYEE);
		employee.setDesignation(designation);
		employee.setTrainingDayEntitlement(designation.getDefaultTrainingDays());
		employee.setTrainingBudget(ANNUAL_BUDGET);
		employee.setManager(manager);
		return employee;
	}

	private void seedHolidays() {
		holiday(2026, 1, 1, "New Year's Day");
		holiday(2026, 2, 17, "Chinese New Year");
		holiday(2026, 2, 18, "Chinese New Year");
		holiday(2026, 3, 21, "Hari Raya Puasa");
		holiday(2026, 4, 3, "Good Friday");
		holiday(2026, 5, 1, "Labour Day");
		holiday(2026, 5, 27, "Hari Raya Haji");
		holiday(2026, 5, 31, "Vesak Day");
		holiday(2026, 6, 1, "Vesak Day (in lieu)");
		holiday(2026, 8, 9, "National Day");
		holiday(2026, 8, 10, "National Day (in lieu)");
		holiday(2026, 11, 8, "Deepavali");
		holiday(2026, 11, 9, "Deepavali (in lieu)");
		holiday(2026, 12, 25, "Christmas Day");
	}

	private void holiday(int year, int month, int day, String name) {
		publicHolidayRepository.save(new PublicHoliday(LocalDate.of(year, month, day), name));
	}
}

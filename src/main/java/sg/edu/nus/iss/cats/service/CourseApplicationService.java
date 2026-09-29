package sg.edu.nus.iss.cats.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.cats.entity.CourseApplication;
import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.ApplicationStatus;
import sg.edu.nus.iss.cats.model.CourseCategory;
import sg.edu.nus.iss.cats.model.SessionType;
import sg.edu.nus.iss.cats.repository.CourseApplicationRepository;
import sg.edu.nus.iss.cats.repository.EmployeeRepository;
import sg.edu.nus.iss.cats.repository.PublicHolidayRepository;
import sg.edu.nus.iss.cats.web.CourseApplicationForm;

/**
 * Days and fees count toward the calendar year of the course start date for
 * Applied, Updated, Approved, and Completed applications. Overlap is checked
 * against Applied, Updated, and Approved applications of the same employee.
 */
@Service
public class CourseApplicationService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
	private static final BigDecimal HALF_DAY = new BigDecimal("0.5");
	private static final Set<ApplicationStatus> COUNTED = EnumSet.of(
			ApplicationStatus.APPLIED,
			ApplicationStatus.UPDATED,
			ApplicationStatus.APPROVED,
			ApplicationStatus.COMPLETED);
	private static final Set<ApplicationStatus> BLOCKING = EnumSet.of(
			ApplicationStatus.APPLIED,
			ApplicationStatus.UPDATED,
			ApplicationStatus.APPROVED);

	private final EmployeeRepository employeeRepository;
	private final CourseApplicationRepository courseApplicationRepository;
	private final PublicHolidayRepository publicHolidayRepository;

	public CourseApplicationService(EmployeeRepository employeeRepository,
			CourseApplicationRepository courseApplicationRepository,
			PublicHolidayRepository publicHolidayRepository) {
		this.employeeRepository = employeeRepository;
		this.courseApplicationRepository = courseApplicationRepository;
		this.publicHolidayRepository = publicHolidayRepository;
	}

	@Transactional
	public CourseApplication submit(Long employeeId, CourseApplicationForm form) {
		Employee employee = requireEmployee(employeeId);
		Draft draft = parse(form, employee, null);
		CourseApplication application = new CourseApplication(
				employee,
				draft.title,
				draft.category,
				draft.provider,
				draft.startDate,
				draft.endDate,
				draft.sessionType,
				draft.fee,
				draft.justification,
				draft.dissemination,
				ApplicationStatus.APPLIED,
				LocalDateTime.now(ZONE));
		return courseApplicationRepository.save(application);
	}

	@Transactional
	public CourseApplication update(Long employeeId, Long applicationId, CourseApplicationForm form) {
		CourseApplication application = requireOwn(employeeId, applicationId);
		if (!canEdit(application.getStatus())) {
			throw new RuleViolationException(List.of("This application can no longer be changed."));
		}
		Draft draft = parse(form, application.getEmployee(), application.getId());
		application.setTitle(draft.title);
		application.setCategory(draft.category);
		application.setProvider(draft.provider);
		application.setStartDate(draft.startDate);
		application.setEndDate(draft.endDate);
		application.setSessionType(draft.sessionType);
		application.setFee(draft.fee);
		application.setJustification(draft.justification);
		application.setDissemination(draft.dissemination);
		application.setStatus(ApplicationStatus.UPDATED);
		return courseApplicationRepository.save(application);
	}

	@Transactional
	public void delete(Long employeeId, Long applicationId) {
		CourseApplication application = requireOwn(employeeId, applicationId);
		if (!canEdit(application.getStatus())) {
			throw new RuleViolationException(List.of("This application can no longer be deleted."));
		}
		application.setStatus(ApplicationStatus.DELETED);
		courseApplicationRepository.save(application);
	}

	@Transactional
	public void cancel(Long employeeId, Long applicationId) {
		CourseApplication application = requireOwn(employeeId, applicationId);
		if (application.getStatus() != ApplicationStatus.APPROVED || !application.getEndDate().isAfter(today())) {
			throw new RuleViolationException(List.of("Only an approved course that has not ended can be cancelled."));
		}
		application.setStatus(ApplicationStatus.CANCELLED);
		courseApplicationRepository.save(application);
	}

	@Transactional
	public void complete(Long employeeId, Long applicationId, String experience) {
		CourseApplication application = requireOwn(employeeId, applicationId);
		if (application.getStatus() != ApplicationStatus.APPROVED || !application.getEndDate().isBefore(today())) {
			throw new RuleViolationException(List.of("You can mark the course completed after the end date."));
		}
		if (experience == null || experience.isBlank()) {
			throw new RuleViolationException(List.of("Experience comments are required."));
		}
		if (experience.trim().length() > 1000) {
			throw new RuleViolationException(List.of("Experience comments must be 1000 characters or fewer."));
		}
		application.setExperienceComment(experience.trim());
		application.setStatus(ApplicationStatus.COMPLETED);
		courseApplicationRepository.save(application);
	}

	@Transactional(readOnly = true)
	public List<CourseApplication> personalHistory(Long employeeId) {
		Employee employee = requireEmployee(employeeId);
		int year = today().getYear();
		return courseApplicationRepository.findByEmployeeAndStartDateBetweenOrderByStartDateDesc(
				employee, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
	}

	@Transactional(readOnly = true)
	public ApplicationDetail detailForEmployee(Long employeeId, Long applicationId) {
		CourseApplication application = requireOwn(employeeId, applicationId);
		LocalDate today = today();
		boolean editable = canEdit(application.getStatus());
		boolean approved = application.getStatus() == ApplicationStatus.APPROVED;
		return new ApplicationDetail(
				application,
				trainingDays(application),
				usage(application.getEmployee(), application.getStartDate().getYear(), null),
				List.of(),
				editable,
				editable,
				approved && application.getEndDate().isAfter(today),
				approved && application.getEndDate().isBefore(today),
				false);
	}

	@Transactional(readOnly = true)
	public Usage usageFor(Long employeeId) {
		Employee employee = requireEmployee(employeeId);
		return usage(employee, today().getYear(), null);
	}

	@Transactional(readOnly = true)
	public List<SubordinateGroup> pendingApprovals(Long managerId) {
		Employee manager = requireEmployee(managerId);
		List<Employee> subordinates = employeeRepository.findByManager(manager);
		if (subordinates.isEmpty()) {
			return List.of();
		}
		List<CourseApplication> pending = courseApplicationRepository.findByEmployeeInAndStatusIn(
				subordinates, EnumSet.of(ApplicationStatus.APPLIED, ApplicationStatus.UPDATED));
		return group(subordinates, pending, false);
	}

	@Transactional(readOnly = true)
	public List<SubordinateGroup> subordinateHistory(Long managerId) {
		Employee manager = requireEmployee(managerId);
		List<Employee> subordinates = employeeRepository.findByManager(manager);
		if (subordinates.isEmpty()) {
			return List.of();
		}
		int year = today().getYear();
		List<CourseApplication> applications = courseApplicationRepository
				.findByEmployeeInAndStartDateBetweenOrderByStartDateDesc(
						subordinates, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
		return group(subordinates, applications, true);
	}

	@Transactional(readOnly = true)
	public ApplicationDetail detailForManager(Long managerId, Long applicationId) {
		CourseApplication application = requireSubordinateApplication(managerId, applicationId);
		boolean pending = application.getStatus() == ApplicationStatus.APPLIED
				|| application.getStatus() == ApplicationStatus.UPDATED;
		return new ApplicationDetail(
				application,
				trainingDays(application),
				usage(application.getEmployee(), application.getStartDate().getYear(), null),
				otherApproved(application),
				false,
				false,
				false,
				false,
				pending);
	}

	@Transactional
	public void decide(Long managerId, Long applicationId, boolean approve, String comment) {
		CourseApplication application = requireSubordinateApplication(managerId, applicationId);
		if (application.getStatus() != ApplicationStatus.APPLIED
				&& application.getStatus() != ApplicationStatus.UPDATED) {
			throw new RuleViolationException(List.of("This application has already been decided."));
		}
		if (comment == null || comment.isBlank()) {
			throw new RuleViolationException(List.of("A reason is required."));
		}
		if (comment.trim().length() > 1000) {
			throw new RuleViolationException(List.of("The reason must be 1000 characters or fewer."));
		}
		application.setManagerComment(comment.trim());
		application.setDecisionAt(LocalDateTime.now(ZONE));
		application.setStatus(approve ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
		courseApplicationRepository.save(application);
	}

	public BigDecimal trainingDays(CourseApplication application) {
		Set<LocalDate> holidays = holidays();
		if (application.getSessionType() == SessionType.HALF_DAY) {
			return HALF_DAY;
		}
		return TrainingDayCalculator.countFullDays(application.getStartDate(), application.getEndDate(), holidays);
	}

	private Draft parse(CourseApplicationForm form, Employee employee, Long excludeId) {
		List<String> errors = new ArrayList<>();
		String title = text(form.getTitle());
		String provider = text(form.getProvider());
		String justification = text(form.getJustification());
		String dissemination = text(form.getDissemination());

		if (title == null) {
			errors.add("Course title is required.");
		} else if (title.length() > 150) {
			errors.add("Course title must be 150 characters or fewer.");
		}
		CourseCategory category = enumValue(CourseCategory.class, form.getCategory());
		if (category == null) {
			errors.add("Course category is required.");
		}
		if (provider == null) {
			errors.add("Training provider is required.");
		} else if (provider.length() > 120) {
			errors.add("Training provider must be 120 characters or fewer.");
		}
		if (justification == null) {
			errors.add("Justification is required.");
		} else if (justification.length() > 1000) {
			errors.add("Justification must be 1000 characters or fewer.");
		}
		if (dissemination != null && dissemination.length() > 1000) {
			errors.add("Work dissemination must be 1000 characters or fewer.");
		}

		LocalDate startDate = parseDate(form.getStartDate(), "Start date", errors);
		LocalDate endDate = parseDate(form.getEndDate(), "End date", errors);
		SessionType sessionType = enumValue(SessionType.class, form.getSessionType());
		if (sessionType == null) {
			errors.add("Session length is required.");
		}

		Set<LocalDate> holidays = holidays();
		if (startDate != null && endDate != null) {
			if (endDate.isBefore(startDate)) {
				errors.add("The end date must be on or after the start date.");
			}
			if (!startDate.isAfter(today())) {
				errors.add("The course must start on a future date.");
			}
			if (!TrainingDayCalculator.isWorkingDay(startDate, holidays)) {
				errors.add("The start date must be a working day.");
			}
			if (!TrainingDayCalculator.isWorkingDay(endDate, holidays)) {
				errors.add("The end date must be a working day.");
			}
		}

		if (sessionType == SessionType.HALF_DAY && category != null && category != CourseCategory.INTERNAL_TRAINING) {
			errors.add("Half-day sessions are allowed for internal training only.");
		}
		if (sessionType == SessionType.HALF_DAY && startDate != null && endDate != null && !startDate.equals(endDate)) {
			errors.add("A half-day session must start and end on the same day.");
		}

		BigDecimal fee = parseFee(form.getFee(), category, errors);
		BigDecimal days = null;
		if (errors.isEmpty() && startDate != null && endDate != null && sessionType != null) {
			days = sessionType == SessionType.HALF_DAY
					? HALF_DAY
					: TrainingDayCalculator.countFullDays(startDate, endDate, holidays);
			checkEntitlement(employee, startDate.getYear(), days, excludeId, errors);
			checkBudget(employee, startDate.getYear(), fee, excludeId, errors);
			checkOverlap(employee, startDate, endDate, excludeId, errors);
		}

		if (!errors.isEmpty()) {
			throw new RuleViolationException(errors);
		}
		return new Draft(title, category, provider, startDate, endDate, sessionType, fee, justification, dissemination);
	}

	private void checkEntitlement(Employee employee, int year, BigDecimal days, Long excludeId, List<String> errors) {
		if (employee.getTrainingDayEntitlement() == null) {
			errors.add("A training-day entitlement has not been set for this year.");
			return;
		}
		Usage usage = usage(employee, year, excludeId);
		if (usage.daysUsed().add(days).compareTo(employee.getTrainingDayEntitlement()) > 0) {
			errors.add("This course exceeds the remaining training-day entitlement.");
		}
	}

	private void checkBudget(Employee employee, int year, BigDecimal fee, Long excludeId, List<String> errors) {
		if (fee.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		if (employee.getTrainingBudget() == null) {
			errors.add("A training budget has not been set for this year.");
			return;
		}
		Usage usage = usage(employee, year, excludeId);
		if (usage.budgetUsed().add(fee).compareTo(employee.getTrainingBudget()) > 0) {
			errors.add("The fee exceeds the remaining training budget.");
		}
	}

	private void checkOverlap(Employee employee, LocalDate start, LocalDate end, Long excludeId, List<String> errors) {
		boolean overlaps = courseApplicationRepository.findByEmployeeAndStatusIn(employee, BLOCKING).stream()
				.filter(application -> excludeId == null || !application.getId().equals(excludeId))
				.anyMatch(application -> !start.isAfter(application.getEndDate()) && !end.isBefore(application.getStartDate()));
		if (overlaps) {
			errors.add("The course period overlaps another active application.");
		}
	}

	private Usage usage(Employee employee, int year, Long excludeId) {
		BigDecimal days = BigDecimal.ZERO;
		BigDecimal fees = BigDecimal.ZERO;
		for (CourseApplication application : courseApplicationRepository.findByEmployeeAndStatusIn(employee, COUNTED)) {
			if (application.getStartDate().getYear() != year) {
				continue;
			}
			if (excludeId != null && application.getId().equals(excludeId)) {
				continue;
			}
			days = days.add(trainingDays(application));
			fees = fees.add(application.getFee());
		}
		return new Usage(days, employee.getTrainingDayEntitlement(), fees, employee.getTrainingBudget());
	}

	private List<CourseApplication> otherApproved(CourseApplication application) {
		Employee manager = application.getEmployee().getManager();
		if (manager == null) {
			return List.of();
		}
		List<Employee> others = employeeRepository.findByManager(manager).stream()
				.filter(employee -> !employee.getId().equals(application.getEmployee().getId()))
				.toList();
		if (others.isEmpty()) {
			return List.of();
		}
		return courseApplicationRepository.findByEmployeeInAndStatusIn(others, EnumSet.of(ApplicationStatus.APPROVED))
				.stream()
				.filter(other -> !application.getStartDate().isAfter(other.getEndDate())
						&& !application.getEndDate().isBefore(other.getStartDate()))
				.sorted(Comparator.comparing(CourseApplication::getStartDate))
				.toList();
	}

	private List<SubordinateGroup> group(List<Employee> subordinates, List<CourseApplication> applications,
			boolean includeEmpty) {
		return subordinates.stream()
				.sorted(Comparator.comparing(Employee::getName))
				.map(employee -> new SubordinateGroup(
						employee.getName(),
						applications.stream()
								.filter(application -> application.getEmployee().getId().equals(employee.getId()))
								.sorted(Comparator.comparing(CourseApplication::getStartDate))
								.toList()))
				.filter(group -> includeEmpty || !group.applications().isEmpty())
				.toList();
	}

	private CourseApplication requireOwn(Long employeeId, Long applicationId) {
		CourseApplication application = courseApplicationRepository.findById(applicationId)
				.orElseThrow(() -> new NotFoundException("Course application not found."));
		if (!application.getEmployee().getId().equals(employeeId)) {
			throw new NotFoundException("Course application not found.");
		}
		return application;
	}

	private CourseApplication requireSubordinateApplication(Long managerId, Long applicationId) {
		CourseApplication application = courseApplicationRepository.findById(applicationId)
				.orElseThrow(() -> new NotFoundException("Course application not found."));
		Employee manager = application.getEmployee().getManager();
		if (manager == null || !manager.getId().equals(managerId)) {
			throw new NotFoundException("Course application not found.");
		}
		return application;
	}

	private Employee requireEmployee(Long employeeId) {
		return employeeRepository.findById(employeeId)
				.orElseThrow(() -> new NotFoundException("Employee not found."));
	}

	private Set<LocalDate> holidays() {
		return publicHolidayRepository.findAll().stream()
				.map(holiday -> holiday.getHolidayDate())
				.collect(java.util.stream.Collectors.toSet());
	}

	private static boolean canEdit(ApplicationStatus status) {
		return status == ApplicationStatus.APPLIED || status == ApplicationStatus.UPDATED;
	}

	private static LocalDate today() {
		return LocalDate.now(ZONE);
	}

	private static String text(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static LocalDate parseDate(String value, String label, List<String> errors) {
		if (value == null || value.isBlank()) {
			errors.add(label + " is required.");
			return null;
		}
		try {
			return LocalDate.parse(value.trim());
		} catch (DateTimeParseException ex) {
			errors.add("Enter a valid " + label.toLowerCase() + ".");
			return null;
		}
	}

	private static BigDecimal parseFee(String value, CourseCategory category, List<String> errors) {
		String text = value == null ? "" : value.trim();
		if (category == CourseCategory.INTERNAL_TRAINING) {
			if (text.isEmpty()) {
				return BigDecimal.ZERO.setScale(2);
			}
		} else if (category != null && text.isEmpty()) {
			errors.add("Course fee is required for this category.");
			return null;
		}
		if (text.isEmpty()) {
			return null;
		}
		try {
			BigDecimal fee = new BigDecimal(text);
			if (fee.scale() > 2) {
				errors.add("Enter the fee with at most two decimal places.");
				return null;
			}
			if (fee.compareTo(BigDecimal.ZERO) < 0) {
				errors.add("Course fee cannot be negative.");
				return null;
			}
			if (category == CourseCategory.INTERNAL_TRAINING && fee.compareTo(BigDecimal.ZERO) > 0) {
				errors.add("Internal training has no course fee.");
				return null;
			}
			if (category != null && category != CourseCategory.INTERNAL_TRAINING && fee.compareTo(BigDecimal.ZERO) <= 0) {
				errors.add("Course fee is required for this category.");
				return null;
			}
			return fee.setScale(2);
		} catch (NumberFormatException ex) {
			errors.add("Enter a valid course fee.");
			return null;
		}
	}

	private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Enum.valueOf(type, value.trim());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private record Draft(
			String title,
			CourseCategory category,
			String provider,
			LocalDate startDate,
			LocalDate endDate,
			SessionType sessionType,
			BigDecimal fee,
			String justification,
			String dissemination) {
	}
}

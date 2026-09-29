package sg.edu.nus.iss.cats.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import sg.edu.nus.iss.cats.model.ApplicationStatus;
import sg.edu.nus.iss.cats.model.CourseCategory;
import sg.edu.nus.iss.cats.model.SessionType;

@Entity
@Table(name = "course_applications")
public class CourseApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(nullable = false, length = 150)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private CourseCategory category;

	@Column(nullable = false, length = 120)
	private String provider;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SessionType sessionType;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal fee;

	@Column(nullable = false, length = 1000)
	private String justification;

	@Column(length = 1000)
	private String dissemination;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ApplicationStatus status;

	@Column(length = 1000)
	private String managerComment;

	@Column(length = 1000)
	private String experienceComment;

	@Column(nullable = false)
	private LocalDateTime submittedAt;

	private LocalDateTime decisionAt;

	protected CourseApplication() {
	}

	public CourseApplication(Employee employee, String title, CourseCategory category, String provider,
			LocalDate startDate, LocalDate endDate, SessionType sessionType, BigDecimal fee, String justification,
			String dissemination, ApplicationStatus status, LocalDateTime submittedAt) {
		this.employee = employee;
		this.title = title;
		this.category = category;
		this.provider = provider;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sessionType = sessionType;
		this.fee = fee;
		this.justification = justification;
		this.dissemination = dissemination;
		this.status = status;
		this.submittedAt = submittedAt;
	}

	public Long getId() {
		return id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CourseCategory getCategory() {
		return category;
	}

	public void setCategory(CourseCategory category) {
		this.category = category;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	public void setSessionType(SessionType sessionType) {
		this.sessionType = sessionType;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public String getDissemination() {
		return dissemination;
	}

	public void setDissemination(String dissemination) {
		this.dissemination = dissemination;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public String getManagerComment() {
		return managerComment;
	}

	public void setManagerComment(String managerComment) {
		this.managerComment = managerComment;
	}

	public String getExperienceComment() {
		return experienceComment;
	}

	public void setExperienceComment(String experienceComment) {
		this.experienceComment = experienceComment;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public LocalDateTime getDecisionAt() {
		return decisionAt;
	}

	public void setDecisionAt(LocalDateTime decisionAt) {
		this.decisionAt = decisionAt;
	}
}

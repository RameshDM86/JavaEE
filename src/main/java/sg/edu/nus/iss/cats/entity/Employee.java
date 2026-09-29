package sg.edu.nus.iss.cats.entity;

import java.math.BigDecimal;

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
import sg.edu.nus.iss.cats.model.Designation;
import sg.edu.nus.iss.cats.model.Role;

@Entity
@Table(name = "employees")
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String userId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 120)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Designation designation;

	@ManyToOne
	@JoinColumn(name = "manager_id")
	private Employee manager;

	@Column(precision = 4, scale = 1)
	private BigDecimal trainingDayEntitlement;

	@Column(precision = 10, scale = 2)
	private BigDecimal trainingBudget;

	protected Employee() {
	}

	public Employee(String userId, String password, String name, String email, Role role) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Designation getDesignation() {
		return designation;
	}

	public void setDesignation(Designation designation) {
		this.designation = designation;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public BigDecimal getTrainingDayEntitlement() {
		return trainingDayEntitlement;
	}

	public void setTrainingDayEntitlement(BigDecimal trainingDayEntitlement) {
		this.trainingDayEntitlement = trainingDayEntitlement;
	}

	public BigDecimal getTrainingBudget() {
		return trainingBudget;
	}

	public void setTrainingBudget(BigDecimal trainingBudget) {
		this.trainingBudget = trainingBudget;
	}
}

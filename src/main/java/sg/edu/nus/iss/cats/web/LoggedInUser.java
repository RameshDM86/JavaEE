package sg.edu.nus.iss.cats.web;

import java.io.Serializable;

import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.Role;

public class LoggedInUser implements Serializable {

	private final Long id;
	private final String userId;
	private final String name;
	private final Role role;

	public LoggedInUser(Long id, String userId, String name, Role role) {
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.role = role;
	}

	public static LoggedInUser from(Employee employee) {
		return new LoggedInUser(employee.getId(), employee.getUserId(), employee.getName(), employee.getRole());
	}

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public Role getRole() {
		return role;
	}
}

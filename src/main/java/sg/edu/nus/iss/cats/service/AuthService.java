package sg.edu.nus.iss.cats.service;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.repository.EmployeeRepository;
import sg.edu.nus.iss.cats.web.LoggedInUser;

@Service
public class AuthService {

	private final EmployeeRepository employeeRepository;

	public AuthService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public LoggedInUser loginEmployeePortal(String userId, String password) {
		return login(userId, password, EnumSet.of(Role.EMPLOYEE, Role.MANAGER));
	}

	public LoggedInUser loginAdminPortal(String userId, String password) {
		return login(userId, password, EnumSet.of(Role.ADMIN));
	}

	private LoggedInUser login(String userId, String password, Set<Role> allowedRoles) {
		if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
			throw new LoginFailedException("User id and password are required.");
		}

		Employee employee = employeeRepository.findByUserId(userId.trim())
				.orElseThrow(() -> new LoginFailedException("Invalid user id or password."));

		if (!employee.getPassword().equals(password)) {
			throw new LoginFailedException("Invalid user id or password.");
		}

		if (!allowedRoles.contains(employee.getRole())) {
			throw new LoginFailedException("This account uses the other login page.");
		}

		return LoggedInUser.from(employee);
	}
}

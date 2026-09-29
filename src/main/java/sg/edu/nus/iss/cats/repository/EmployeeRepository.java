package sg.edu.nus.iss.cats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.Role;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByUserId(String userId);

	List<Employee> findByManager(Employee manager);

	List<Employee> findByRole(Role role);
}

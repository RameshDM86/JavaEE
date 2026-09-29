package sg.edu.nus.iss.cats.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.cats.entity.CourseApplication;
import sg.edu.nus.iss.cats.entity.Employee;
import sg.edu.nus.iss.cats.model.ApplicationStatus;

public interface CourseApplicationRepository extends JpaRepository<CourseApplication, Long> {

	List<CourseApplication> findByEmployeeAndStartDateBetweenOrderByStartDateDesc(
			Employee employee, LocalDate start, LocalDate end);

	List<CourseApplication> findByEmployeeAndStatusIn(Employee employee, Collection<ApplicationStatus> statuses);

	List<CourseApplication> findByEmployeeInAndStatusIn(
			Collection<Employee> employees, Collection<ApplicationStatus> statuses);

	List<CourseApplication> findByEmployeeInAndStartDateBetweenOrderByStartDateDesc(
			Collection<Employee> employees, LocalDate start, LocalDate end);
}

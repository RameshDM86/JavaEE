package sg.edu.nus.iss.cats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.cats.entity.PublicHoliday;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

	List<PublicHoliday> findAllByOrderByHolidayDateAsc();
}

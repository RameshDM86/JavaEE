package sg.edu.nus.iss.cats.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "public_holidays")
public class PublicHoliday {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private LocalDate holidayDate;

	@Column(nullable = false, length = 80)
	private String name;

	protected PublicHoliday() {
	}

	public PublicHoliday(LocalDate holidayDate, String name) {
		this.holidayDate = holidayDate;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getHolidayDate() {
		return holidayDate;
	}

	public void setHolidayDate(LocalDate holidayDate) {
		this.holidayDate = holidayDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

package sg.edu.nus.iss.cats.model;

import java.math.BigDecimal;

public enum Designation {
	ADMINISTRATIVE("Administrative", new BigDecimal("5.0")),
	PROFESSIONAL("Professional", new BigDecimal("10.0"));

	private final String label;
	private final BigDecimal defaultTrainingDays;

	Designation(String label, BigDecimal defaultTrainingDays) {
		this.label = label;
		this.defaultTrainingDays = defaultTrainingDays;
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getDefaultTrainingDays() {
		return defaultTrainingDays;
	}
}

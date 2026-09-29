package sg.edu.nus.iss.cats.model;

public enum SessionType {
	FULL_DAY("Full day"),
	HALF_DAY("Half day");

	private final String label;

	SessionType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}

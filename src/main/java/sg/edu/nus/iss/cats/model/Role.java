package sg.edu.nus.iss.cats.model;

public enum Role {
	ADMIN("Administrator"),
	MANAGER("Manager"),
	EMPLOYEE("Employee");

	private final String label;

	Role(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}

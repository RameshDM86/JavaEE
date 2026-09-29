package sg.edu.nus.iss.cats.model;

public enum ApplicationStatus {
	APPLIED("Applied"),
	UPDATED("Updated"),
	DELETED("Deleted"),
	APPROVED("Approved"),
	REJECTED("Rejected"),
	CANCELLED("Cancelled"),
	COMPLETED("Completed");

	private final String label;

	ApplicationStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}

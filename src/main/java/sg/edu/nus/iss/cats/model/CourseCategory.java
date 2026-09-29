package sg.edu.nus.iss.cats.model;

public enum CourseCategory {
	INTERNAL_TRAINING("Internal training"),
	EXTERNAL_COURSE("External course"),
	PROFESSIONAL_CERTIFICATION("Professional certification");

	private final String label;

	CourseCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}

package sg.edu.nus.iss.cats.service;

import java.util.List;

public class RuleViolationException extends RuntimeException {

	private final List<String> errors;

	public RuleViolationException(List<String> errors) {
		super(String.join(" ", errors));
		this.errors = List.copyOf(errors);
	}

	public List<String> getErrors() {
		return errors;
	}
}

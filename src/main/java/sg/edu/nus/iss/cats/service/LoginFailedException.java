package sg.edu.nus.iss.cats.service;

public class LoginFailedException extends RuntimeException {

	public LoginFailedException(String message) {
		super(message);
	}
}

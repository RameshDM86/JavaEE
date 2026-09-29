package sg.edu.nus.iss.cats.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sg.edu.nus.iss.cats.service.AuthService;
import sg.edu.nus.iss.cats.service.LoginFailedException;

@Controller
public class EmployeeLoginController {

	private final AuthService authService;

	public EmployeeLoginController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/employee/login")
	public String form(HttpSession session) {
		if (session.getAttribute(SessionKeys.USER) != null) {
			return "redirect:/home";
		}
		return "employee-login";
	}

	@PostMapping("/employee/login")
	public String login(@RequestParam(defaultValue = "") String userId,
			@RequestParam(defaultValue = "") String password,
			HttpSession session,
			Model model) {
		try {
			LoggedInUser user = authService.loginEmployeePortal(userId, password);
			session.setAttribute(SessionKeys.USER, user);
			return "redirect:/home";
		} catch (LoginFailedException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("userId", userId);
			return "employee-login";
		}
	}
}

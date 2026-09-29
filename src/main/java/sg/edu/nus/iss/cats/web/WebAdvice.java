package sg.edu.nus.iss.cats.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.service.NotFoundException;

@ControllerAdvice
public class WebAdvice {

	@ModelAttribute
	public void currentUser(HttpSession session, Model model) {
		LoggedInUser user = session == null ? null : (LoggedInUser) session.getAttribute(SessionKeys.USER);
		model.addAttribute("user", user);
		boolean employee = user != null && (user.getRole() == Role.EMPLOYEE || user.getRole() == Role.MANAGER);
		model.addAttribute("showEmployeeMenu", employee);
		model.addAttribute("showManagerMenu", user != null && user.getRole() == Role.MANAGER);
		model.addAttribute("showAdminMenu", user != null && user.getRole() == Role.ADMIN);
	}

	@ExceptionHandler(NotFoundException.class)
	public String notFound(NotFoundException exception, Model model) {
		model.addAttribute("message", exception.getMessage());
		return "not-found";
	}
}

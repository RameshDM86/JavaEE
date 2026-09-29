package sg.edu.nus.iss.cats.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import sg.edu.nus.iss.cats.model.Role;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index(HttpSession session) {
		if (session.getAttribute(SessionKeys.USER) != null) {
			return "redirect:/home";
		}
		return "redirect:/employee/login";
	}

	@GetMapping("/home")
	public String home(HttpSession session, Model model) {
		LoggedInUser user = (LoggedInUser) session.getAttribute(SessionKeys.USER);
		model.addAttribute("user", user);
		model.addAttribute("showEmployeeMenu", user.getRole() == Role.EMPLOYEE || user.getRole() == Role.MANAGER);
		model.addAttribute("showManagerMenu", user.getRole() == Role.MANAGER);
		model.addAttribute("showAdminMenu", user.getRole() == Role.ADMIN);
		return "home";
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		LoggedInUser user = (LoggedInUser) session.getAttribute(SessionKeys.USER);
		session.invalidate();
		if (user != null && user.getRole() == Role.ADMIN) {
			return "redirect:/admin/login";
		}
		return "redirect:/employee/login";
	}
}

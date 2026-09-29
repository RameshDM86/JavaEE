package sg.edu.nus.iss.cats.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.service.ApplicationDetail;
import sg.edu.nus.iss.cats.service.CourseApplicationService;
import sg.edu.nus.iss.cats.service.RuleViolationException;

@Controller
public class ManagerCourseController {

	private final CourseApplicationService courseApplicationService;

	public ManagerCourseController(CourseApplicationService courseApplicationService) {
		this.courseApplicationService = courseApplicationService;
	}

	@GetMapping("/manager/approvals")
	public String approvals(HttpSession session, Model model) {
		if (!isManager(session)) {
			return "redirect:/home";
		}
		model.addAttribute("groups", courseApplicationService.pendingApprovals(current(session).getId()));
		return "approvals";
	}

	@GetMapping("/manager/history")
	public String history(HttpSession session, Model model) {
		if (!isManager(session)) {
			return "redirect:/home";
		}
		model.addAttribute("groups", courseApplicationService.subordinateHistory(current(session).getId()));
		model.addAttribute("year", java.time.LocalDate.now(java.time.ZoneId.of("Asia/Singapore")).getYear());
		return "subordinate-history";
	}

	@GetMapping("/manager/courses/{id}")
	public String detail(@PathVariable Long id, HttpSession session, Model model) {
		if (!isManager(session)) {
			return "redirect:/home";
		}
		ApplicationDetail detail = courseApplicationService.detailForManager(current(session).getId(), id);
		model.addAttribute("detail", detail);
		model.addAttribute("managerView", true);
		return "application";
	}

	@PostMapping("/manager/courses/{id}/decide")
	public String decide(@PathVariable Long id, HttpSession session, @RequestParam String decision,
			@RequestParam(defaultValue = "") String comment, RedirectAttributes redirectAttributes) {
		if (!isManager(session)) {
			return "redirect:/home";
		}
		try {
			boolean approve = "approve".equals(decision);
			courseApplicationService.decide(current(session).getId(), id, approve, comment);
			redirectAttributes.addFlashAttribute("message",
					approve ? "Application approved." : "Application rejected.");
		} catch (RuleViolationException ex) {
			redirectAttributes.addFlashAttribute("errors", ex.getErrors());
		}
		return "redirect:/manager/courses/" + id;
	}

	private boolean isManager(HttpSession session) {
		LoggedInUser user = current(session);
		return user != null && user.getRole() == Role.MANAGER;
	}

	private LoggedInUser current(HttpSession session) {
		return (LoggedInUser) session.getAttribute(SessionKeys.USER);
	}
}

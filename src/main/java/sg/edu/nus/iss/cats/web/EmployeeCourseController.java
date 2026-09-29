package sg.edu.nus.iss.cats.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sg.edu.nus.iss.cats.entity.CourseApplication;
import sg.edu.nus.iss.cats.model.CourseCategory;
import sg.edu.nus.iss.cats.model.Role;
import sg.edu.nus.iss.cats.model.SessionType;
import sg.edu.nus.iss.cats.service.ApplicationDetail;
import sg.edu.nus.iss.cats.service.CourseApplicationService;
import sg.edu.nus.iss.cats.service.RuleViolationException;

@Controller
public class EmployeeCourseController {

	private final CourseApplicationService courseApplicationService;

	public EmployeeCourseController(CourseApplicationService courseApplicationService) {
		this.courseApplicationService = courseApplicationService;
	}

	@GetMapping("/courses/new")
	public String newForm(HttpSession session, Model model) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new CourseApplicationForm());
		}
		prepareForm(session, model, null);
		return "apply";
	}

	@PostMapping("/courses")
	public String submit(HttpSession session, @ModelAttribute("form") CourseApplicationForm form, Model model,
			RedirectAttributes redirectAttributes) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		try {
			CourseApplication application = courseApplicationService.submit(current(session).getId(), form);
			redirectAttributes.addFlashAttribute("message", "Application submitted. Status is Applied.");
			return "redirect:/courses/" + application.getId();
		} catch (RuleViolationException ex) {
			model.addAttribute("errors", ex.getErrors());
			prepareForm(session, model, null);
			return "apply";
		}
	}

	@GetMapping("/courses/history")
	public String history(HttpSession session, Model model) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		model.addAttribute("applications", courseApplicationService.personalHistory(current(session).getId()));
		model.addAttribute("year", java.time.LocalDate.now(java.time.ZoneId.of("Asia/Singapore")).getYear());
		return "history";
	}

	@GetMapping("/courses/{id}")
	public String detail(@PathVariable Long id, HttpSession session, Model model) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		ApplicationDetail detail = courseApplicationService.detailForEmployee(current(session).getId(), id);
		model.addAttribute("detail", detail);
		model.addAttribute("managerView", false);
		return "application";
	}

	@GetMapping("/courses/{id}/edit")
	public String editForm(@PathVariable Long id, HttpSession session, Model model) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		ApplicationDetail detail = courseApplicationService.detailForEmployee(current(session).getId(), id);
		if (!detail.canUpdate()) {
			return "redirect:/courses/" + id;
		}
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", CourseApplicationForm.from(detail.application()));
		}
		prepareForm(session, model, id);
		return "apply";
	}

	@PostMapping("/courses/{id}")
	public String update(@PathVariable Long id, HttpSession session, @ModelAttribute("form") CourseApplicationForm form,
			Model model, RedirectAttributes redirectAttributes) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		try {
			courseApplicationService.update(current(session).getId(), id, form);
			redirectAttributes.addFlashAttribute("message", "Application updated.");
			return "redirect:/courses/" + id;
		} catch (RuleViolationException ex) {
			model.addAttribute("errors", ex.getErrors());
			prepareForm(session, model, id);
			return "apply";
		}
	}

	@PostMapping("/courses/{id}/delete")
	public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		try {
			courseApplicationService.delete(current(session).getId(), id);
			redirectAttributes.addFlashAttribute("message", "Application deleted.");
		} catch (RuleViolationException ex) {
			redirectAttributes.addFlashAttribute("errors", ex.getErrors());
		}
		return "redirect:/courses/" + id;
	}

	@PostMapping("/courses/{id}/cancel")
	public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		try {
			courseApplicationService.cancel(current(session).getId(), id);
			redirectAttributes.addFlashAttribute("message", "Application cancelled.");
		} catch (RuleViolationException ex) {
			redirectAttributes.addFlashAttribute("errors", ex.getErrors());
		}
		return "redirect:/courses/" + id;
	}

	@PostMapping("/courses/{id}/complete")
	public String complete(@PathVariable Long id, HttpSession session, @RequestParam(defaultValue = "") String experience,
			RedirectAttributes redirectAttributes) {
		if (!canApply(session)) {
			return "redirect:/home";
		}
		try {
			courseApplicationService.complete(current(session).getId(), id, experience);
			redirectAttributes.addFlashAttribute("message", "Course marked as completed.");
		} catch (RuleViolationException ex) {
			redirectAttributes.addFlashAttribute("errors", ex.getErrors());
		}
		return "redirect:/courses/" + id;
	}

	private void prepareForm(HttpSession session, Model model, Long applicationId) {
		model.addAttribute("categories", CourseCategory.values());
		model.addAttribute("sessionTypes", SessionType.values());
		model.addAttribute("usage", courseApplicationService.usageFor(current(session).getId()));
		model.addAttribute("applicationId", applicationId);
	}

	private boolean canApply(HttpSession session) {
		LoggedInUser user = current(session);
		return user.getRole() == Role.EMPLOYEE || user.getRole() == Role.MANAGER;
	}

	private LoggedInUser current(HttpSession session) {
		return (LoggedInUser) session.getAttribute(SessionKeys.USER);
	}
}

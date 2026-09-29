package sg.edu.nus.iss.cats.service;

import java.math.BigDecimal;
import java.util.List;

import sg.edu.nus.iss.cats.entity.CourseApplication;

public record ApplicationDetail(
		CourseApplication application,
		BigDecimal trainingDays,
		Usage usage,
		List<CourseApplication> otherApproved,
		boolean canUpdate,
		boolean canDelete,
		boolean canCancel,
		boolean canComplete,
		boolean canDecide) {
}

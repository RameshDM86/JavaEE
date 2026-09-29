package sg.edu.nus.iss.cats.web;

import sg.edu.nus.iss.cats.entity.CourseApplication;

public class CourseApplicationForm {

	private String title;
	private String category;
	private String provider;
	private String startDate;
	private String endDate;
	private String sessionType;
	private String fee;
	private String justification;
	private String dissemination;

	public static CourseApplicationForm from(CourseApplication application) {
		CourseApplicationForm form = new CourseApplicationForm();
		form.setTitle(application.getTitle());
		form.setCategory(application.getCategory().name());
		form.setProvider(application.getProvider());
		form.setStartDate(application.getStartDate().toString());
		form.setEndDate(application.getEndDate().toString());
		form.setSessionType(application.getSessionType().name());
		form.setFee(application.getFee().toPlainString());
		form.setJustification(application.getJustification());
		form.setDissemination(application.getDissemination());
		return form;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getSessionType() {
		return sessionType;
	}

	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public String getDissemination() {
		return dissemination;
	}

	public void setDissemination(String dissemination) {
		this.dissemination = dissemination;
	}
}

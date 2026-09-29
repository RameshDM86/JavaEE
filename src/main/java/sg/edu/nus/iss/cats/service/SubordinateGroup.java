package sg.edu.nus.iss.cats.service;

import java.util.List;

import sg.edu.nus.iss.cats.entity.CourseApplication;

public record SubordinateGroup(String employeeName, List<CourseApplication> applications) {
}

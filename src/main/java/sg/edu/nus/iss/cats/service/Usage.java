package sg.edu.nus.iss.cats.service;

import java.math.BigDecimal;

public record Usage(BigDecimal daysUsed, BigDecimal dayEntitlement, BigDecimal budgetUsed, BigDecimal annualBudget) {
}

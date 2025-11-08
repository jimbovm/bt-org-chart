package com.github.jimbovm.bt.orgchart;

/**
 * Represents an employee record as parsed from an org chart text file.
 */
public record EmployeeRecord(
		int id,
		String name,
		int manager) {
}

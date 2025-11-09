package com.github.jimbovm.bt.orgchart;

import com.google.common.base.Optional;

/**
 * Represents an employee record as parsed from an org chart text file.
 */
public record Employee(
		int id,
		String name,
		int manager) {

	public boolean isChief() {
		return this.id == this.manager;
	}

	public boolean manages(Employee employee) {
		return (employee == null) || (this.manager == employee.id());
	}

	public boolean manages(Optional<Employee> employee) {
		return (employee.orNull() == null) || (this.manager() == employee.get().id());
	}
}

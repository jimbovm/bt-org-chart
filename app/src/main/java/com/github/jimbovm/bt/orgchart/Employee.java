package com.github.jimbovm.bt.orgchart;

import java.util.Optional;

/**
 * Represents an employee record as parsed from an org chart text file.
 */
public record Employee(
		int id,
		String name,
		int manager) {

	/**
	 * Check if this employee is the chief of the organisation, that is, they are
	 * their "own manager".
	 * 
	 * @return True if this employee's ID equals their manager's, false otherwise.
	 */
	public boolean isChief() {
		return this.id == this.manager;
	}

	/**
	 * Check if an employee is the manager of this one.
	 * 
	 * @param employee An Employee instance, who may be this employee's manager.
	 * @return True if this employee manages the employee given as a parameter.
	 */
	public boolean manages(Employee employee) {
		return (employee == null) || (this.manager == employee.id());
	}

	/**
	 * Check if an employee is the manager of this one.
	 * 
	 * @param employee An Optional wrapping an Employee instance, who may be this
	 *                 employee's manager.
	 * @return True if this employee manages the employee given as a parameter.
	 */
	public boolean manages(Optional<Employee> employee) {
		return (employee.isEmpty()) || (this.manager() == employee.get().id());
	}

	/**
	 * Return a string representation of an Employee instance.
	 * 
	 * @return A string representation of the form "[employee name] (employee ID)".
	 */
	public String toString() {
		return String.format("%s (%d)", this.name, this.id);
	}
}

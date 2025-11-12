package com.github.jimbovm.bt.orgchart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Tree representation of an organisational hierarchy.
 * 
 * This is an n-ary tree made of nested hierarchies. The employee attribute of a
 * hierarchy represents either a manager of a sub-hierarchy that reports to
 * them or, if the hierarchy is a leaf (i.e. the employee has no direct reports)
 * an individual contributor.
 */
public final class Hierarchy {

	/** Logger implementation. */
	private static Logger logger = Logger.getGlobal();

	/** The employee represented by this node in the hierarchy. */
	private Optional<Employee> employee = Optional.empty();

	/** Direct reports to the employee at this node; may be empty. */
	private List<Hierarchy> reports = new ArrayList<Hierarchy>();

	/**
	 * Create a Hierarchy from a list of employees.
	 * 
	 * It is assumed that there is a single company chief, i.e. only one employee
	 * who "reports to themselves" and is answerable to no one, and the organisation
	 * is not run on a "primus inter pares" basis.
	 * 
	 * @param employees A list of Employee records.
	 * @return The organisational hierarchy with the chief at the root.
	 * @throws IllegalArgumentException in the event of a list of employees that
	 *                                  contains multiple chiefs, i.e. employees
	 *                                  that "manage themselves".
	 */
	public static Hierarchy of(List<Employee> employees) throws IllegalArgumentException {

		Objects.requireNonNullElse(employees, List.of());
		if (employees.isEmpty()) {
			return new Hierarchy();
		}

		List<Employee> chiefs = employees.stream()
				.filter(employee -> employee.id() == employee.manager())
				.toList();
		if (chiefs.size() > 1) {
			final var message1 = "Multiple chiefs; only one employee may be answerable to no one (this is an assumption; see README)\n";
			final var multipleChiefs = employees.stream()
					.filter(employee -> employee.id() == employee.manager()).toList();
			final var message2 = String.format("Employees %s are all chiefs", multipleChiefs.toString());
			throw new IllegalArgumentException(message1 + message2);
		}

		// if we're here, we have one chief as required
		Employee chief = chiefs.get(0);
		var orgRoot = buildHierarchy(employees, chief);

		return orgRoot;
	}

	/**
	 * Recursively build the organisational hierarchy.
	 * 
	 * @param employees A list of Employee records.
	 * @param manager   An employee who may have direct reports.
	 * @return The complete organisational hierarchy in tree form.
	 */
	private static Hierarchy buildHierarchy(List<Employee> employees, Employee manager) {

		var hierarchy = new Hierarchy();
		hierarchy.setEmployee(manager);

		for (var employee : employees) {

			final var employeeIsReport = employee.manager() == manager.id();
			final var employeeIsBoss = employee.id() == manager.id();

			if (employeeIsReport && !(employeeIsBoss)) {

				logger.fine(String.format("Employee %s (%d) reports to %s (%d)", employee.name(),
						employee.id(), manager.name(), manager.id()));

				var reports = buildHierarchy(employees, employee);
				hierarchy.addReport(reports);
			}
		}

		return hierarchy;
	}

	/**
	 * Create a new, empty hierarchy.
	 * Internal use only. Hierarchy must be substantiated using of().
	 */
	private Hierarchy() {

	}

	/**
	 * Return the employee at the head of the hierarchy.
	 * 
	 * @return An instance of Employee.
	 */
	public Optional<Employee> getEmployee() {
		return employee;
	}

	/**
	 * Return the employee at the head of the hierarchy's direct reports in the form
	 * of sub-hierarchies.
	 * 
	 * @return A list of Hierarchy instances, which is empty if the employee is not
	 *         a manager, i.e. no other employees report to them.
	 */
	public List<Hierarchy> getReports() {
		return reports;
	}

	/**
	 * Set the employee at the head of the hierarchy.
	 * 
	 * @param employee The employee to set as head of this hierarchy.
	 */
	public void setEmployee(Employee employee) {
		this.employee = Optional.of(employee);
	}

	/**
	 * Add a new report to the employee at this node.
	 * 
	 * @param hierarchy A hierarchy rooted at a direct report to the employee at
	 *                  this node.
	 */
	public void addReport(Hierarchy hierarchy) {
		this.reports.add(hierarchy);
	}

	/**
	 * Set a full list of reports for the employee at the head of this hierarchy.
	 * 
	 * @param reports A list of Hierarchy instances.
	 */
	public void setReports(List<Hierarchy> reports) {
		this.reports = reports;
	}

	/**
	 * Return whether an employee reports directly the employee at the head of this
	 * hierarchy.
	 * 
	 * @param employee The employee to check for direct reporting.
	 * @return True if the employee is a direct report, false otherwise.
	 */
	public boolean isDirectReport(Employee employee) {
		return (employee == null) || this.reports.stream().anyMatch(report -> report.getEmployee().isPresent()
				&& report.getEmployee().get().equals(employee));
	}

	/**
	 * Helper method for toString().
	 * 
	 * @param hierarchy The hierarchy to generate a recursive representation of.
	 * @param builder   A StringBuilder instance.
	 * @param depth     The depth of the nested hierarchy.
	 * @return A StringBuilder instance into which a tree representation has been
	 *         rendered.
	 */
	private StringBuilder toStringHelper(Hierarchy hierarchy, StringBuilder builder,
			int depth) {

		builder.append("\t".repeat(depth) + hierarchy.getEmployee().get().toString()
				+ System.getProperty("line.separator"));

		for (var report : hierarchy.getReports()) {
			toStringHelper(report, builder, depth + 1);
		}

		return builder;
	}

	/**
	 * Returns a representation of the hierarchy as a nested tree.
	 * 
	 * @return A tree representation of the hierarchy
	 */
	public String toString() {

		var builder = new StringBuilder();

		if (this.employee.isEmpty()) {
			return "Empty organisation";
		}

		return this.toStringHelper(this, builder, 0).toString();
	}
}

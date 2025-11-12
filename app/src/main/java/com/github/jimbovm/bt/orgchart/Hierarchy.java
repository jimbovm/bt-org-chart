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
	 * @throws IllegalArgumentException
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
	 * 
	 * @param employee
	 */
	private Hierarchy() {

	}

	public Optional<Employee> getEmployee() {
		return employee;
	}

	public List<Hierarchy> getReports() {
		return reports;
	}

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

	public void setReports(List<Hierarchy> reports) {
		this.reports = reports;
	}

	public boolean isDirectReport(Employee employee) {
		return (employee == null) || this.reports.stream().anyMatch(report -> report.getEmployee().isPresent()
				&& report.getEmployee().get().equals(employee));
	}

	private StringBuilder toStringHelper(Hierarchy hierarchy, StringBuilder builder,
			int depth) {

		builder.append("\t".repeat(depth) + hierarchy.getEmployee().get().toString()
				+ System.getProperty("line.separator"));

		for (var report : hierarchy.getReports()) {
			toStringHelper(report, builder, depth + 1);
		}

		return builder;
	}

	public String toString() {

		var builder = new StringBuilder();

		if (this.employee.isEmpty()) {
			return "Empty organisation";
		}

		return this.toStringHelper(this, builder, 0).toString();
	}
}

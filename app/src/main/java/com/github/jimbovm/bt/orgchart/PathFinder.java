package com.github.jimbovm.bt.orgchart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Encapsulates functionality for finding a the highest-ranked common manager in
 * an organisation hierarchy.
 * 
 * This problem is equivalent to finding the "lowest common ancestor" in a tree,
 * and a simple LCA algorithm is used to find the desired output.
 */
public final class PathFinder {

	/** The default logger. */
	private Logger logger = Logger.getGlobal();

	/** The organisational hierarchy. */
	private Hierarchy hierarchy = Hierarchy.of(new ArrayList<>());

	/**
	 * The cached shortest path between the two employees with which the instance
	 * was constructed.
	 */
	private List<Employee> shortestPath = new ArrayList<>();

	/** The cached highest common manager. */
	private Optional<Employee> highestCommonManager;

	/**
	 * Create a new pathfinder for a supplied hierarchy.
	 * 
	 * @param hierarchy The hierarchy to search for a path between two employees
	 *                  within.
	 */
	public PathFinder(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * Use the Lowest Common Ancestor algorithm to find the paths from the big boss
	 * to each employee, then determine the point in the hierarchy where these paths
	 * diverge, i.e. their highest-ranked common manager.
	 * 
	 * @param employee1 The first employee to find a path to the highest-ranked
	 *                  manager starting from.
	 * @param employee2 The second employee to find a path to the highest-ranked
	 *                  manager starting from.
	 */
	public void findShortestPath(Employee employee1, Employee employee2) {

		// use cached values if present, or compute anew if not
		if ((this.shortestPath != null) && (this.highestCommonManager != null)) {
			return;
		}

		logger.info("No cached values, computing highest common manager from scratch");

		List<Employee> pathToEmployee1 = findPathTo(this.hierarchy, employee1);
		logger.info(String.format(
				"Path from big boss to employee 1: %s", pathToEmployee1.toString()));

		List<Employee> pathToEmployee2 = findPathTo(this.hierarchy, employee2);
		logger.info(String.format(
				"Path from big boss to employee 2: %s", pathToEmployee2.toString()));

		// find the divergent point
		var distance = 0;
		while ((distance < pathToEmployee1.size()) && (distance < pathToEmployee2.size()) &&
				pathToEmployee1.get(distance) == pathToEmployee2.get(distance)) {
			distance++;
		}

		var shortestPath = new ArrayList<Employee>();
		shortestPath.addAll(pathToEmployee1.subList(distance - 1, pathToEmployee1.size()));

		// element 0 is currently the highest common manager
		this.highestCommonManager = Optional.of(shortestPath.get(0));

		// reverse in-place; we want the path from employee 1 up to the root
		Collections.reverse(shortestPath);

		shortestPath.addAll(pathToEmployee2.subList(distance, pathToEmployee2.size()));

		this.shortestPath = shortestPath;
	}

	/**
	 * Given a hierarchy, recursively find the path between the manager at the head
	 * (root) and a given employee.
	 * 
	 * @param hierarchy The hierarchy to search, which may be a sub-hierarchy of a
	 *                  larger one.
	 * @param employee  The employee, to which to find a path from that at the head
	 *                  of the hierarchy.
	 * @return A list of employees giving a path between the employee at the head of
	 *         the hierarchy and the second parameter.
	 */
	private List<Employee> findPathTo(Hierarchy hierarchy, Employee employee) {

		logger.fine(String.format("Finding path from %s to %s", hierarchy.getEmployee().get().toString(),
				employee.toString()));

		// base case; if we have found the target, return a singleton list
		if (hierarchy.getEmployee().orElseGet(null).equals(employee)) {
			return new ArrayList<Employee>(List.of(hierarchy.getEmployee().get()));
		}

		// otherwise use the general case
		for (var report : hierarchy.getReports()) {

			var path = findPathTo(report, employee);

			if (path.isEmpty() == false) {

				path.add(0, hierarchy.getEmployee().get());
				logger.info(String.format("Returning path list %s", path.toString()));
				return path;
			}
		}

		logger.fine("Returning empty path list");
		return new ArrayList<>();
	}

	/**
	 * Return a string representation of the path between two employers found,
	 * representing a path through the organisation hierarchy (a traversal of a
	 * tree).
	 * 
	 * @return String representations of employees joined by arrows to indicate the
	 *         direction of traversal.
	 */
	public String toString() {

		if (this.shortestPath.size() == 1) {
			return this.shortestPath.get(0).toString();
		}

		final var LEFT_ARROW = "<-";
		final var RIGHT_ARROW = "->";

		var builder = new StringBuilder();

		// append the left-hand strings first, all except the last with arrows after
		// them
		List<Employee> leftEmployees = new ArrayList<>();
		leftEmployees.addAll(this.shortestPath.stream()
				.takeWhile(employee -> employee != this.highestCommonManager.get()).toList());

		leftEmployees.forEach(
				employee -> builder.append(String.format("%s %s ", employee.toString(), RIGHT_ARROW)));

		// now add the boss
		builder.append(this.highestCommonManager.get().toString());

		// finally, append the right-hand strings, all but the first with arrows before
		// them
		List<Employee> rightStrings = new ArrayList<>();
		rightStrings.addAll(this.shortestPath.stream()
				.filter(employee -> (!leftEmployees.contains(employee))
						&& (employee != this.highestCommonManager.get()))
				.toList());

		rightStrings.forEach(
				employee -> builder.append(String.format(" %s %s", LEFT_ARROW, employee.toString())));

		// done!
		return builder.toString();
	}
}

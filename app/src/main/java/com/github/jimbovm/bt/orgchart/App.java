package com.github.jimbovm.bt.orgchart;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.github.jimbovm.bt.orgchart.parser.Parser;

/**
 * Executable class for the org chart app.
 */
public final class App {

	/** Return code to exit with on a successful result. */
	private static final int EXIT_SUCCESS = 0;

	/** Return code to exit on in the event of an error. */
	private static final int EXIT_FAILURE = 1;

	/** Command line argument index to the path to the input file. */
	private static final int FILE_PATH = 0;

	/** Command line argument index to the first employee name. */
	private static final int EMPLOYEE_1 = 1;

	/** Command line argument index to the second employee name. */
	private static final int EMPLOYEE_2 = 2;

	/** Cached path to the input file. */
	private static String filePath;

	/** The normalized name of the first employee. */
	private static String firstEmployeeName;

	/** The normalized name of the second employee. */
	private static String secondEmployeeName;

	/** The global logger. */
	private static Logger logger;

	private static void loggingSetup() throws IOException {
		logger = Logger.getGlobal();
		final var loggingPropertiesStream = App.class.getResourceAsStream("/logging.properties");
		LogManager.getLogManager().readConfiguration(loggingPropertiesStream);
	}

	/**
	 * Normalize a name by replacing all runs of whitespace with a single space,
	 * 
	 * @param name The name to normalize.
	 * @return The normalized name.
	 */
	public static String normalizeName(String name) {

		var whitespaceReplacementPattern = Pattern.compile("\s+",
				Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
		var matcher = whitespaceReplacementPattern.matcher(name);

		var whitespaceNormalizedString = matcher.replaceAll(name);
		return whitespaceNormalizedString.strip().replaceAll("\\s+", " ").toLowerCase();
	}

	/**
	 * Checks if a list of Employee instances has at least one instance of an
	 * employee with a supplied name.
	 * 
	 * @param employees A list of Employee instances.
	 * @param name      A name, which is normalized, which the employee list is
	 *                  search for.
	 * @return True if the condition is met, false otherwise.
	 */
	public static boolean containsName(List<Employee> employees, String name) {
		return employees.stream().anyMatch(employee -> normalizeName(employee.name()) == name);
	}

	/**
	 * Set up state using the command line arguments.
	 * 
	 * @param args The command line argument array.
	 */
	public static void argumentsSetup(String[] args) {

		filePath = args[FILE_PATH];

		if (args.length != 3) {
			System.err.println("Wrong number of arguments.");
			System.err.println("Usage: java -jar app.jar [input file] [employee name] [employee name]");
			System.exit(EXIT_FAILURE);
		}

		firstEmployeeName = normalizeName(args[EMPLOYEE_1]);
		secondEmployeeName = normalizeName(args[EMPLOYEE_2]);

		logger.info("Reading file " + filePath);
		logger.info(String.format("Finding shortest path between \"%s\" (\"%s\") and \"%s\" (\"%s\")",
				firstEmployeeName,
				args[EMPLOYEE_1], secondEmployeeName, args[EMPLOYEE_2]));
	}

	/**
	 * Check if looked-up employees exist in the input file, and exit if not.
	 * 
	 * @param args      The command line arguments.
	 * @param employee1 An Optional possibly wrapping the first employee.
	 * @param employee2 An Optional possibly wrapping the second employee.
	 */
	public static void checkEmployeesFound(String[] args, Optional<Employee> employee1,
			Optional<Employee> employee2) {
		final var employee1NotFound = employee1.isEmpty();
		final var employee2NotFound = employee2.isEmpty();

		if (employee1NotFound || employee2NotFound) {
			System.err.println(String.format("Employee %s (%s) not found in input file %s",
					firstEmployeeName,
					args[EMPLOYEE_1], filePath));
			System.err.println(String.format("Employee %s (%s) not found in input file %s",
					secondEmployeeName,
					args[EMPLOYEE_2], filePath));
			System.exit(EXIT_FAILURE);
		}
	}

	/**
	 * Main application entry point.
	 * 
	 * @param args The command line arguments; input file path, employee 1 and
	 *             employee 2, in that order.
	 * @throws Exception in the event of a miscellaneous error.
	 */
	public static void main(String[] args) throws Exception {

		loggingSetup();
		argumentsSetup(args);

		try {
			List<Employee> employees = Parser.parse(filePath);
			Hierarchy hierarchy = Hierarchy.of(employees);

			List<Employee> employees1 = employees.stream()
					.filter(employee -> normalizeName(employee.name())
							.equalsIgnoreCase(firstEmployeeName))
					.toList();

			List<Employee> employees2 = employees.stream()
					.filter(employee -> normalizeName(employee.name())
							.equalsIgnoreCase(secondEmployeeName))
					.toList();

			if (employees1.isEmpty() || employees2.isEmpty()) {
				System.err.println("One or more supplied employee names not found.");
				System.exit(EXIT_FAILURE);
			}

			// if we're here, we have valid input
			for (var employee1 : employees1) {
				for (var employee2 : employees2) {
					PathFinder pathFinder = new PathFinder(hierarchy);
					logger.info(String.format("Searching for path between %s and %s",
							employee1.toString(), employee2.toString()));
					pathFinder.findShortestPath(employee1, employee2);
					System.out.println(pathFinder.toString());
				}
			}

			System.exit(EXIT_SUCCESS);

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(EXIT_FAILURE);
		}
	}
}

package com.github.jimbovm.bt.orgchart.parser;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.github.jimbovm.bt.orgchart.EmployeeRecord;

/**
 * Parser for org chart text files.
 */
public final class Parser {

	/** Regular expression for a valid line. */
	private static final Pattern ENTRY_PATTERN;

	/** Logger implementation. */
	private static Logger logger;

	static {
		ENTRY_PATTERN = Pattern.compile(
				"^\\s*\\|\\s*(?<id>\\d+)\\s*\\|\\s*(?<name>[\\-\\w\\s]+)\\s*\\|\\s*(?<manager>\\d*)\\s*\\|\\s*$",
				Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE);
		logger = Logger.getGlobal();
		// logger.addHandler(new ConsoleHandler());
	}

	/**
	 * Parse a single employee record.
	 * 
	 * If the manager field is blank, it will be set to the employee's own ID (i.e.
	 * they are their "own manager").
	 * 
	 * @param line The line to parse.
	 * @return An instance of EmployeeRecord representing the parsed record.
	 * @throws IllegalArgumentException if the line cannot be parsed according to
	 *                                  the format.
	 */
	public static EmployeeRecord parseLine(String line) throws IllegalArgumentException {

		var matcher = ENTRY_PATTERN.matcher(line);
		matcher.find();

		final int id;
		try {
			id = Integer.parseInt(matcher.group("id"));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Malformed line", e);
		}

		final String name = matcher.group("name").strip();
		if (name.isEmpty() || name.isBlank()) {
			throw new IllegalArgumentException("Malformed line: name cannot be blank");
		}

		final int manager;
		try {
			manager = (matcher.group("manager").isEmpty() || matcher.group("manager").isBlank()) ? id
					: Integer.parseInt(matcher.group("manager"));
		} catch (Exception e) {
			throw new IllegalArgumentException("Malformed line", e);
		}

		logger.info(String.format("From line \"%s\" read id: %d, name: %s, manager: %d", line, id, name, manager));
		return new EmployeeRecord(id, name, manager);
	}
}

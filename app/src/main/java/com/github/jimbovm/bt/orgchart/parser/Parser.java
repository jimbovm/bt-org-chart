package com.github.jimbovm.bt.orgchart.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.jimbovm.bt.orgchart.Employee;

/**
 * Parser for org chart text files.
 */
public final class Parser {

	/** Regular expression for a valid file header. */
	private static final Pattern HEADER_PATTERN;

	/** Regular expression for a valid line. */
	private static final Pattern ENTRY_PATTERN;

	/** Logger implementation. */
	private static Logger logger;

	static {
		HEADER_PATTERN = Pattern.compile(
				"^\\s*\\|\\s*Employee ID\\s*\\|\\s*Name\\s*\\|\\s*Manager ID\\s*\\|\\s*$",
				Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE);
		ENTRY_PATTERN = Pattern.compile(
				"^\\s*\\|\\s*(?<id>\\-?\\d+)\\s*\\|\\s*(?<name>[\\'\\-\\w\\s]+)\\s*\\|\\s*(?<manager>(\\-?\\d+)?)\\s*\\|\\s*$",
				Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE);
		logger = Logger.getGlobal();
	}

	/**
	 * Return a stream from an InputStream for lazy line-by-line reading.
	 * 
	 * @param inputStream The InputStream from which to read.
	 * @return A Stream<String> to a reader over the passed InputStream.
	 * @throws IOException if there is a problem accessing the InputStream.
	 */
	private static Stream<String> getStream(InputStream inputStream) throws IOException {

		Objects.requireNonNull(inputStream);
		var reader = new BufferedReader(new InputStreamReader(inputStream));
		return reader.lines();
	}

	/**
	 * Return a stream from an InputStream for lazy line-by-line reading.
	 * 
	 * @param filePath The path to a file from which to read.
	 * @return A Stream<String> to a reader over the file passed.
	 * @throws IOException if there is a problem accessing the file.
	 */
	private static Stream<String> getFileAsStream(String filePath) throws IOException {

		Objects.requireNonNull(filePath);
		final var path = Path.of(filePath);
		logger.info(String.format("Opening org chart file %s", filePath));
		return Files.lines(path, StandardCharsets.UTF_8);
	}

	/**
	 * Parse a single employee record.
	 * 
	 * If the manager field is blank, it will be set to the employee's own ID (i.e.
	 * they are the head of the company and thus their "own manager").
	 * 
	 * @param line The line to parse.
	 * @return An instance of EmployeeRecord representing the parsed record.
	 * @throws IllegalArgumentException if the line cannot be parsed according to
	 *                                  the format.
	 */
	public static Employee parseLine(String line) throws IllegalArgumentException {

		var matcher = ENTRY_PATTERN.matcher(line);
		final var validMatch = matcher.find();

		if (validMatch == false) {
			throw new IllegalArgumentException(String.format("Malformed line: %s", line));
		}

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

		logger.info(String.format("From line \"%s\" read id: %d, name: %s, manager: %d", line, id, name,
				manager));
		return new Employee(id, name, manager);
	}

	/**
	 * Parse all lines from an instance of Stream<String>.
	 * 
	 * @param stream The stream from which to parse.
	 * @return A List of EmployeeRecord instances.
	 */
	private static List<Employee> parseLines(Stream<String> stream) {

		List<Employee> records = stream
				.skip(1) // ignore the header
				.map(String::trim) // strip whitespace (defensive)
				.filter((line) -> !(line.isEmpty() || line.isBlank())) // ignore blank lines
				.map(Parser::parseLine) // lazy line parse
				.toList(); // finalize

		logger.info(String.format("Parsed %d records", records.size()));
		return records;
	}

	private static boolean hasValidHeader(Stream<String> stream) {

		final String firstLine = stream.findFirst().orElseGet(null);
		if (firstLine == null) {
			return false;
		}

		final var headerMatcher = HEADER_PATTERN.matcher(firstLine);

		logger.info(String.format("Parsed %s header \"%s\"", headerMatcher.matches() ? "valid" : "invalid",
				firstLine));

		return headerMatcher.matches();
	}

	/**
	 * Parse an employee file from an InputStream.
	 * 
	 * @param inputStream The stream from which to parse.
	 * @return A List of EmployeeRecord instances.
	 * @throws IOException if there is a problem accessing the stream.
	 */
	public static List<Employee> parse(InputStream inputStream) throws IOException {

		inputStream.mark(0);
		var stream = getStream(inputStream);

		if (hasValidHeader(stream)) {
			inputStream.reset();
			stream = getStream(inputStream);
			return parseLines(stream);
		} else {
			throw new IOException("Malformed input file: no header or header format incorrect");
		}
	}

	/**
	 * Parse an employee file from a file on the filesystem.
	 * 
	 * @param filePath The path to the file from which to parse.
	 * @return A List of EmployeeRecord instances.
	 * @throws IOException if there is a problem accessing the file.
	 */
	public static List<Employee> parse(String filePath) throws IOException {

		var stream = getFileAsStream(filePath);

		if (hasValidHeader(stream)) {
			stream = getFileAsStream(filePath);
			return parseLines(stream);
		} else {
			throw new IOException("Malformed input file: no header or header format incorrect");
		}
	}
}

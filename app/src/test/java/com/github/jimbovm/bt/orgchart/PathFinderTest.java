package com.github.jimbovm.bt.orgchart;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.jimbovm.bt.orgchart.parser.Parser;

/**
 * Tests for the PathFinder class.
 */
public final class PathFinderTest {

	static final String resourcePath = "/koopas.txt";
	InputStream inputStream;
	List<Employee> employees;
	Hierarchy hierarchy;

	@BeforeEach
	void setup() throws Exception {
		this.inputStream = this.getClass().getResourceAsStream(PathFinderTest.resourcePath);
		this.employees = Parser.parse(this.inputStream);
		this.hierarchy = Hierarchy.of(employees);
	}

	/**
	 * 
	 * @param resourcePath     The path to the input file in the class's resource
	 *                         bundle.
	 * @param employee1Name    The name of the first of two employees to find a path
	 *                         between.
	 * @param employee2Name    The name of the second of two employees to find a
	 *                         path between.
	 * @param expectedToString The correct toString() output of the PathFinder
	 *                         instance expected.
	 * @throws Exception in the event of any unexpected error.
	 */
	@ParameterizedTest
	@CsvSource({
			"Bowser Jr, Bowser, Bowser Jr (1) -> Bowser (0)",
			"Pom-Pom, Chargin Chuck, Pom-Pom (11) -> Morton (7) <- Boom-Boom (10) <- Chargin Chuck (700)",
			"Kammy, Roy, Kammy (-2) -> Kamek (-1) -> Bowser (0) <- Roy (3)",
			"Koopa Paratroopa, Kamek, Koopa Paratroopa (180) -> Roy (3) -> Bowser (0) <- Kamek (-1)",
			"Bowser, Bowser, Bowser (0)",
			"Morton, Morton, Morton (7)",
			"Chargin Chuck, Morton, Chargin Chuck (700) -> Boom-Boom (10) -> Morton (7)",
	})
	void testPathfinding(String employee1Name, String employee2Name, String expectedToString)
			throws Exception {
		PathFinder pathFinder = new PathFinder(hierarchy);

		var employee1NameNormalized = App.normalizeName(employee1Name);
		var employee2NameNormalized = App.normalizeName(employee2Name);

		Optional<Employee> employee1 = employees.stream().filter(employee -> App.normalizeName(employee.name())
				.equals(employee1NameNormalized)).findFirst();
		Optional<Employee> employee2 = employees.stream().filter(employee -> App.normalizeName(employee.name())
				.equals(employee2NameNormalized)).findFirst();

		pathFinder.findShortestPath(employee1.get(), employee2.get());

		assertEquals(expectedToString, pathFinder.toString());
	}

	@CsvSource({
			"Hammer Bro, Hammer Bro",
			"Koopa Troopa, Koopa Troopa"
	})
	@ParameterizedTest
	void testDuplicateNames(String firstEmployeeName, String secondEmployeeName) {

		final List<String> validOutputsKT = List.of("Koopa Troopa (100)", "Koopa Troopa (107)",
				"Koopa Troopa (100) -> Roy (3) <- Koopa Troopa (171)",
				"Koopa Troopa (171) -> Roy (3) <- Koopa Troopa (100)");

		final List<String> validOutputsHB = List.of("Hammer Bro (200)", "Hammer Bro (201)",
				"Hammer Bro (200) -> Boom-Boom (10) <- Hammer Bro (201)",
				"Hammer Bro (201) -> Boom-Boom (10) <- Hammer Bro (200)");

		List<String> validOutputs;

		if (firstEmployeeName.equals("Koopa Troopa")) {
			validOutputs = validOutputsKT;
		} else {
			validOutputs = validOutputsHB;
		}

		List<Employee> employees1 = employees.stream()
				.filter(employee -> App.normalizeName(employee.name())
						.equalsIgnoreCase(firstEmployeeName))
				.toList();

		List<Employee> employees2 = employees.stream()
				.filter(employee -> App.normalizeName(employee.name())
						.equalsIgnoreCase(secondEmployeeName))
				.toList();

		List<String> outputs = List.of();

		for (var employee1 : employees1) {
			for (var employee2 : employees2) {
				PathFinder pathFinder = new PathFinder(hierarchy);
				pathFinder.findShortestPath(employee1, employee2);
				outputs.add(pathFinder.toString());
			}
		}

		for (var output : outputs) {
			assertTrue(validOutputs.contains(output));
		}
	}
}

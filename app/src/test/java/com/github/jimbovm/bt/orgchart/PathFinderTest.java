package com.github.jimbovm.bt.orgchart;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.jimbovm.bt.orgchart.parser.Parser;

/**
 * Tests for the PathFinder class.
 */
public final class PathFinderTest {

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
			"/koopas.txt, Bowser Jr, Bowser, Bowser Jr (1) -> Bowser (0)",
			"/koopas.txt, Pom-Pom, Chargin Chuck, Pom-Pom (11) -> Morton (7) <- Boom-Boom (10) <- Chargin Chuck (700)",
			"/koopas.txt, Kammy, Roy, Kammy (-2) -> Kamek (-1) -> Bowser (0) <- Roy (3)",
			"/koopas.txt, Koopa Paratroopa, Kamek, Koopa Paratroopa (180) -> Roy (3) -> Bowser (0) <- Kamek (-1)",
			"/koopas.txt, Bowser, Bowser, Bowser (0)",
			"/koopas.txt, Morton, Morton, Morton (7)",
			"/koopas.txt, Chargin Chuck, Morton, Chargin Chuck (700) -> Boom-Boom (10) -> Morton (7)",
	})
	void testPathfinding(String resourcePath, String employee1Name, String employee2Name, String expectedToString)
			throws Exception {

		var inputStream = this.getClass().getResourceAsStream(resourcePath);
		List<Employee> employees = Parser.parse(inputStream);
		Hierarchy hierarchy = Hierarchy.of(employees);
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
}

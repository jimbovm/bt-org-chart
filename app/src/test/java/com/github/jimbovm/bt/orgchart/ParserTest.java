package com.github.jimbovm.bt.orgchart;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.jimbovm.bt.orgchart.parser.Parser;

/**
 * Test functionality for parsing employee records from input files.
 */
public class ParserTest {

	/**
	 * Test parsing of a single line.
	 * 
	 * @param line            The line to test parsing.
	 * @param expectedId      The expected employee ID.
	 * @param expectedName    The expected employee name.
	 * @param expectedManager The expected ID of the employee's manager.
	 * @throws Exception
	 */
	@ParameterizedTest
	@CsvSource({
			"|0|Bowser||, 0, Bowser, 0",
			" | 0 | Bowser | |, 0, Bowser, 0",
			"|0 |  Bowser	|	|, 0, Bowser, 0",
			" 	| 1| Kamek	 | 0 |	, 1, Kamek, 0",
			" 	| 2| Boom-Boom	 | 0 |	, 2, Boom-Boom, 0",
			"|5| 	 Koopa Troopa  	| 2 |, 5, Koopa Troopa, 2"
	})
	void testParseLine(String line, int expectedId, String expectedName, int expectedManager) throws Exception {

		EmployeeRecord employee = Parser.parseLine(line);
		assertEquals(expectedId, employee.id());
		assertEquals(expectedName, employee.name());
		assertEquals(expectedManager, employee.manager());
	}
}
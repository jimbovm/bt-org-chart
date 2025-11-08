package com.github.jimbovm.bt.orgchart;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
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

	/**
	 * Test parsing a complete org chart file, using a test resource.
	 */
	@Test
	void testParseFile() throws Exception {
		final List<EmployeeRecord> expectedRecords = List.of(
				new EmployeeRecord(1, "Dangermouse", 1),
				new EmployeeRecord(2, "Gonzo the Great", 1),
				new EmployeeRecord(3, "Invisible Woman", 1),
				new EmployeeRecord(6, "Black Widow", 2),
				new EmployeeRecord(12, "Hit Girl", 3),
				new EmployeeRecord(15, "Super Ted", 3),
				new EmployeeRecord(16, "Batman", 6),
				new EmployeeRecord(17, "Catwoman", 6));

		InputStream inputStream = this.getClass().getResourceAsStream("/superheroes.txt");
		{
			var records = Parser.parse(inputStream);
			assertEquals(8, records.size());
			for (int i = 0; i < records.size(); i++) {
				assertEquals(expectedRecords.get(i), records.get(i));
			}
		}
	}
}
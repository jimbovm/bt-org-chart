package com.github.jimbovm.bt.orgchart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public final class HierarchyTest {

	private final List<Employee> employees = List.of(
			new Employee(0, "Princess Peach", 0),
			new Employee(2, "Toad", 0),
			new Employee(10, "Toadette", 0),
			new Employee(64, "Kooper", 2));

	private final List<Employee> multipleChiefs = List.of(
			new Employee(1, "Chief 1", 1),
			new Employee(2, "Chief 2", 2));

	@Test
	void testBuildHierarchy() throws Exception {

		final var PEACH_ORGANISATION = Hierarchy.of(employees);
		final var PEACH = employees.get(0);
		final var TOAD = employees.get(1);
		final var TEAM_TOAD = PEACH_ORGANISATION.getReports().get(0);
		final var TOADETTE = employees.get(2);
		final var KOOPER = employees.get(3);

		System.out.println(PEACH_ORGANISATION.toString());

		assertEquals(PEACH, PEACH_ORGANISATION.getEmployee().get());
		assertEquals(TOAD, TEAM_TOAD.getEmployee().get());

		assertTrue(PEACH_ORGANISATION.isDirectReport(TOAD));
		assertTrue(PEACH_ORGANISATION.isDirectReport(TOADETTE));
		assertTrue(TEAM_TOAD.isDirectReport(KOOPER));
		assertFalse(TEAM_TOAD.isDirectReport(TOADETTE));
	}

	@SuppressWarnings("unused")
	@Test
	void testFailOnMultipleChiefs() throws Exception {

		assertThrows(IllegalArgumentException.class, () -> {
			Hierarchy hierarchy = Hierarchy.of(multipleChiefs);
		});
	}

	@Test
	void testToStringOnEmpty() throws Exception {

		assertEquals("Empty organisation", Hierarchy.of(List.of()).toString());
	}
}
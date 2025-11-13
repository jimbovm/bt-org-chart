# BT Org Chart Submission

## System requirements

* Java 17 JDK and any operating system capable of running it
* Gradle 9 (managed automatically by the `gradlew` script)
* JUnit 5 Jupiter

## Assumptions

1. There can be only one "chief"/"big boss", or head of the organisation, and one head of any sub-hierarchy; that is, only a single employee at the root of the organisation hierarchy can be accountable to no one, and there is no job sharing between any employees who have direct reports.
1. There may be no duplicate employee IDs in the input, and an employee may not have more than one manager (however, distinct employees may have the same name; see below).
1. Any integer, including zero and negative numbers, may be an employee ID.
1. Names of employees may only contain Unicode alphabet characters, hyphens and apostrophes.
1. Input files must have a valid header of the form given in the example; the header names must be exactly as given in the example, but otherwise the header is whitespace-insensitive.
1. When a hierarchy contains duplicate employee names, the program should print **all** paths between **all** employees with the same name. (The problem statement says that "you don't have to show" all such paths and "at least one" path should be printed, which I have interpreted as allowing this; I wished to include this functionality for completeness.)

## Building the software

### 1. Clone the repository.

```
git clone https://github.com/jimbovm/bt-org-chart
```

### 2. Change directory into the repository root.

```
cd bt-org-chart
```

If you wish, you may adjust the default logging level in `app/src/main/resources/logging.properties` before building. The default is SEVERE, which effectively means no logging is output to the console. Changing it to INFO will produce more output, and setting it to FINE will produce even more verbose output.

### 3. Use Gradle to build the project:

```
./gradlew.bat build
```

on Windows, or

```
./gradlew build
```

on any other operating system.

This will automatically run the tests and generate a JaCoCo test coverage report and complete Javadocs as part of the build process.

### 4. Execute the resulting jar:

```
java -jar app/build/libs/app.jar [input file] [employee 1] [employee 2]
```

Employee names containing spaces or non-alphabetic characters may need to be escaped or surrounded in double quotes, as per how your shell parses input.

## Licence

SPDX-License-Identifier: MIT

See `LICENSE.md`.
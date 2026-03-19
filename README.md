# Allure Scraper

A command-line tool that extracts `@Issue` and `@Description` annotations from Java test files and generates CSV reports for integration with JIRA and test management systems.

## Features

- **Annotation Extraction**: Parses `@Issue` and `@Description` annotations from Allure test framework
- **Flexible Scanning**: Can scan any directory, including external repositories
- **CSV Output**: Generates CSV reports with columns: `className`, `methodName`, `issueKey`, `description`
- **Multiple Config Options**: Supports both CLI arguments and YAML configuration files
- **JavaParser Integration**: Uses JavaParser library for robust Java source code parsing

## Requirements

- Java 21 or higher
- Maven 3.8+

## Building the Project

Build the project using Maven to create a runnable JAR file:

```bash
mvn clean package
```

This creates a fat JAR at:
```
target/allure-scraper-1.0-SNAPSHOT-all.jar
```

## Usage

### Running via CLI

The scraper can be run using the JAR file:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar [OPTIONS]
```

### Running with Maven

You can also run directly with Maven:

```bash
mvn exec:java -Dexec.mainClass="org.example.cli.ScraperCommand" -Dexec.args="[OPTIONS]"
```

## CLI Parameters

| Short | Long           | Description                                      | Default                        |
|-------|----------------|--------------------------------------------------|--------------------------------|
| `-s`  | `--source`    | Source directory containing test Java files     | `./src/test/java`             |
| `-o`  | `--output`    | Output path for the CSV report                  | `./target/jira/allure-report.csv` |
| `-p`  | `--pattern`   | File pattern to search (e.g., `*.java`)          | `*.java`                      |
| `-r`  | `--recursive` | Search subdirectories recursively               | `true`                        |
| `-c`  | `--config`    | Path to configuration file (YAML)               | (none)                        |
| `-h`  | `--help`      | Show help message                                |                                |
| `-V`  | `--version`   | Show version information                         |                                |

## Configuration File

### YAML Format

Create a YAML configuration file (e.g., `config.yaml`):

```yaml
sourceDirectory: ./src/test/java
outputFile: ./target/jira/allure-report.csv
filePattern: "*.java"
recursive: true
```

### Using Config File

Pass the config file using the `-c` or `--config` option:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar --config=config.yaml
```

**Note**: CLI arguments take precedence over config file values. This allows you to override specific settings while using a config file as the base.

## Example Commands

### Basic Usage (Default)

Scans the default test directory and generates a report:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar
```

### Scan Current Project

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --source=./src/test/java \
    --output=./allure-report.csv
```

### Scan External Repository

To scan tests in a different repository, provide the absolute path to the test directory:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --source=C:/Users/Developer/my-other-project/src/test/java \
    --output=C:/Users/Developer/my-other-project/test-report.csv
```

On Linux/Mac:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --source=/home/developer/projects/my-other-project/src/test/java \
    --output=/home/developer/projects/my-other-project/test-report.csv
```

### Using Custom File Pattern

Only scan files matching a specific pattern (e.g., integration tests):

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --source=./src/integrationTest/java \
    --pattern=*IntegrationTest.java \
    --output=integration-tests-report.csv
```

### Non-Recursive Scan

Disable recursive directory search:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --source=./src/test/java \
    --recursive=false \
    --output=flat-report.csv
```

### Combine Config File with CLI Overrides

Use a config file but override specific settings via CLI:

```bash
java -jar target/allure-scraper-1.0-SNAPSHOT-all.jar \
    --config=config.yaml \
    --output=custom-output.csv
```

### With Maven

```bash
mvn exec:java -Dexec.mainClass="org.example.cli.ScraperCommand" \
    -Dexec.args="--source=./src/test/java --output=report.csv"
```

## Output Format

The generated CSV file contains the following columns:

| Column       | Description                          |
|--------------|--------------------------------------|
| `className`  | Full qualified class name           |
| `methodName` | Test method name                     |
| `issueKey`   | JIRA issue key from @Issue          |
| `description`| Description from @Description       |

### Example Output

```csv
className,methodName,issueKey,description
com.example.tests.LoginTest,testSuccessfulLogin,PROJ-123,Verify user can login with valid credentials
com.example.tests.LoginTest,testInvalidPassword,PROJ-124,Verify login fails with wrong password
com.example.tests.LoginTest,testPasswordReset,PROJ-125,Verify password reset flow
```

## Annotations Supported

The tool extracts the following Allure annotations:

- `@Issue` - Links a test to a JIRA issue
- `@Description` - Provides a human-readable description of the test

Example usage in test code:

```java
import io.qameta.allure.Issue;
import io.qameta.allure.Description;

import org.junit.jupiter.api.Test;

class LoginTest {

    @Test
    @Issue("PROJ-123")
    @Description("Verify user can login with valid credentials")
    void testSuccessfulLogin() {
        // test implementation
    }

    @Test
    @Issue("PROJ-124")
    @Description("Verify login fails with invalid password")
    void testInvalidPassword() {
        // test implementation
    }
}
```

## Configuration Priority

Configuration values are applied in the following order (highest to lowest priority):

1. **CLI arguments** - Override all other settings
2. **Config file** - YAML configuration
3. **Default values** - Built-in defaults

## Project Structure

```
allure-scraper/
├── pom.xml                 # Maven configuration
├── config.yaml             # Example YAML configuration
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           ├── Main.java                 # Entry point
│   │           ├── cli/ScraperCommand.java   # CLI command definition
│   │           ├── config/ConfigLoader.java  # Configuration loader
│   │           ├── model/                    # Data models
│   │           ├── parser/                   # Annotation parsers
│   │           ├── report/                   # CSV report generator
│   │           └── scanner/                  # Source code scanner
│   └── test/
│       └── java/                             # Test code
└── target/                                     # Build output
```

## License

This project is provided as-is for test automation purposes.

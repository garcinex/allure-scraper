package org.example.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.model.ScraperConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * CLI command dla Allure Scraper.
 * Używa Picocli do parsowania argumentów linii komend.
 */
@Command(
        name = "allure-scraper",
        description = "Narzędzie do ekstrakcji danych z adnotacji Allure i generowania raportów CSV",
        mixinStandardHelpOptions = true,
        version = "1.0-SNAPSHOT",
        footer = """
                Przykłady użycia:
                  allure-scraper --source=./src/test/java --output=report.csv
                  allure-scraper --config=config.properties
                  allure-scraper -s /path/to/tests -o /path/to/output.csv
                """
)
@Getter
@Slf4j
public class ScraperCommand implements Callable<Integer> {

    // Domyślne wartości - używane do wykrywania czy użytkownik podał jawną wartość
    private static final String DEFAULT_SOURCE = "./src/test/java";
    private static final String DEFAULT_OUTPUT = "./target/jira/allure-report.csv";
    private static final String DEFAULT_PATTERN = "*.java";
    private static final String DEFAULT_RECURSIVE = "true";

    @Option(
            names = {"-s", "--source"},
            description = "Ścieżka do katalogu z kodem źródłowym testów (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = DEFAULT_SOURCE
    )
    private String sourceDirectory;

    @Option(
            names = {"-o", "--output"},
            description = "Ścieżka wyjściowa dla pliku CSV (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = DEFAULT_OUTPUT
    )
    private String outputFile;

    @Option(
            names = {"-p", "--pattern"},
            description = "Wzorzec nazw plików do przeszukania (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = DEFAULT_PATTERN
    )
    private String filePattern;

    @Option(
            names = {"-r", "--recursive"},
            description = "Czy rekursywnie przeszukiwać podkatalogi (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = DEFAULT_RECURSIVE
    )
    private Boolean recursive;

    @Option(
            names = {"-c", "--config"},
            description = "Ścieżka do pliku konfiguracyjnego (properties lub YAML)",
            defaultValue = ""
    )
    private String configFile;

    /**
     * Sprawdza czy opcja sourceDirectory została jawnie podana przez użytkownika.
     */
    public boolean isSourceDirectorySet() {
        return !DEFAULT_SOURCE.equals(sourceDirectory);
    }

    /**
     * Sprawdza czy opcja outputFile została jawnie podana przez użytkownika.
     */
    public boolean isOutputFileSet() {
        return !DEFAULT_OUTPUT.equals(outputFile);
    }

    /**
     * Sprawdza czy opcja filePattern została jawnie podana przez użytkownika.
     */
    public boolean isFilePatternSet() {
        return !DEFAULT_PATTERN.equals(filePattern);
    }

    /**
     * Sprawdza czy opcja recursive została jawnie podana przez użytkownika.
     */
    public boolean isRecursiveSet() {
        return recursive != null;
    }

    /**
     * Sprawdza czy opcja configFile została jawnie podana przez użytkownika.
     */
    public boolean isConfigFileSet() {
        return configFile != null && !configFile.isEmpty();
    }

    // Gettery dla pól (potrzebne przez ConfigLoader) - wygenerowane przez Lombok/Picocli
    // sourceDirectory, outputFile, filePattern, recursive, configFile - są generowane automatycznie

    /**
     * Wywoływane przez Picocli po sparsowaniu argumentów.
     * Zwraca kod wyjścia (0 = sukces).
     * Note: Konfiguracja jest finalnie ładana w Main.java przez ConfigLoader.
     */
    @Override
    public Integer call() {
        // Nie打印ujemy konfiguracji tutaj - Main.java ładuje config z pliku
        // i drukuje poprawną konfigurację po złączeniu z plikiem config.yaml
        log.debug("CLI parsowanie zakończone, configFile: {}", configFile);
        return 0;
    }

    /**
     * Pobiera konfigurację z argumentów CLI.
     */
    public ScraperConfig toScraperConfig() {
        return ScraperConfig.builder()
                .sourceDirectory(sourceDirectory)
                .outputFile(outputFile)
                .filePattern(filePattern)
                .recursive(recursive)
                .build();
    }

    /**
     * Pobiera plik konfiguracyjny podany w argumentach CLI.
     */
    public String getConfigFile() {
        return configFile.isEmpty() ? null : configFile;
    }

    /**
     * Main entry point dla uruchomienia z linii komend.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ScraperCommand()).execute(args);
        System.exit(exitCode);
    }
}

package org.example.cli;

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
@Slf4j
public class ScraperCommand implements Callable<Integer> {

    @Option(
            names = {"-s", "--source"},
            description = "Ścieżka do katalogu z kodem źródłowym testów (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = "./src/test/java"
    )
    private String sourceDirectory;

    @Option(
            names = {"-o", "--output"},
            description = "Ścieżka wyjściowa dla pliku CSV (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = "./target/jira/allure-report.csv"
    )
    private String outputFile;

    @Option(
            names = {"-p", "--pattern"},
            description = "Wzorzec nazw plików do przeszukania (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = "*.java"
    )
    private String filePattern;

    @Option(
            names = {"-r", "--recursive"},
            description = "Czy rekursywnie przeszukiwać podkatalogi (domyślnie: ${DEFAULT-VALUE})",
            defaultValue = "true"
    )
    private boolean recursive;

    @Option(
            names = {"-c", "--config"},
            description = "Ścieżka do pliku konfiguracyjnego (properties lub YAML)",
            defaultValue = ""
    )
    private String configFile;

    /**
     * Wywoływane przez Picocli po sparsowaniu argumentów.
     * Zwraca kod wyjścia (0 = sukces).
     */
    @Override
    public Integer call() {
        log.info("Uruchomienie Allure Scraper");
        log.debug("sourceDirectory: {}", sourceDirectory);
        log.debug("outputFile: {}", outputFile);
        log.debug("filePattern: {}", filePattern);
        log.debug("recursive: {}", recursive);
        log.debug("configFile: {}", configFile);

        // TODO: Tu będzie logika scrapera
        System.out.println("Konfiguracja:");
        System.out.println("  Źródło: " + sourceDirectory);
        System.out.println("  Wyjście: " + outputFile);
        System.out.println("  Wzorzec: " + filePattern);
        System.out.println("  Rekurencja: " + recursive);
        
        if (!configFile.isEmpty()) {
            System.out.println("  Plik konfiguracyjny: " + configFile);
        }

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

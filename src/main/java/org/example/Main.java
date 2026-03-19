package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.cli.ScraperCommand;
import org.example.config.ConfigLoader;
import org.example.model.ScraperConfig;
import org.example.model.TestData;
import org.example.parser.AnnotationParser;
import org.example.parser.IssueExtractor;
import org.example.report.CsvReportGenerator;
import org.example.scanner.JavaSourceScanner;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa aplikacji Allure Scraper.
 * Łączy wszystkie komponenty w spójny przepływ pracy.
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        // Parsuj argumenty CLI
        ScraperCommand command = new ScraperCommand();
        int exitCode = new CommandLine(command).execute(args);
        
        if (exitCode != 0) {
            System.exit(exitCode);
        }

        // Pobierz konfigurację
        ConfigLoader configLoader = new ConfigLoader();
        ScraperConfig config = configLoader.load(
                command.getConfigFile(),
                command
        );

        // Uruchom scraper
        try {
            int result = runScraper(config);
            System.exit(result);
        } catch (Exception e) {
            log.error("Błąd podczas działania scrapera", e);
            System.exit(1);
        }
    }

    /**
     * Główna logika scrapera.
     */
    private static int runScraper(ScraperConfig config) throws IOException {
        log.info("=== Allure Scraper - rozpoczęcie ===");
        log.info("Konfiguracja: source={}, output={}, pattern={}, recursive={}",
                config.getSourceDirectory(),
                config.getOutputFile(),
                config.getFilePattern(),
                config.isRecursive());

        // Krok 1: Skanuj pliki Java
        log.info("Krok 1: Skanowanie plików Java...");
        JavaSourceScanner scanner = new JavaSourceScanner(config);
        List<Path> javaFiles = scanner.scan();
        
        if (javaFiles.isEmpty()) {
            log.warn("Nie znaleziono plików Java w katalogu: {}", config.getSourceDirectory());
            return 0;
        }
        
        log.info("Znaleziono {} plików do przetworzenia", javaFiles.size());

        // Krok 2: Parsuj adnotacje z każdego pliku
        log.info("Krok 2: Parsowanie adnotacji Allure...");
        AnnotationParser parser = new AnnotationParser();
        List<TestData> allTestData = new ArrayList<>();
        
        for (Path file : javaFiles) {
            List<TestData> testData = parser.parse(file);
            allTestData.addAll(testData);
            log.debug("Przetworzono {} - znaleziono {} testów", file.getFileName(), testData.size());
        }
        
        log.info("Łącznie znaleziono {} testów z adnotacjami", allTestData.size());

        // Krok 3: Wyciągnij issue z opisów
        log.info("Krok 3: Ekstrakcja issue z opisów...");
        IssueExtractor issueExtractor = new IssueExtractor();
        List<TestData> processedData = issueExtractor.process(allTestData);
        
        log.info("Po przetworzeniu: {} wpisów (z uwzględnieniem wielu issue na test)", processedData.size());

        // Krok 4: Generuj raport CSV
        log.info("Krok 4: Generowanie raportu CSV...");
        CsvReportGenerator reportGenerator = new CsvReportGenerator();
        reportGenerator.generate(processedData, config.getOutputFile());

        log.info("=== Allure Scraper - zakończenie ===");
        log.info("Raport zapisany do: {}", config.getOutputFile());

        return 0;
    }
}

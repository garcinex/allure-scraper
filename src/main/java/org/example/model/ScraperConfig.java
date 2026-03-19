package org.example.model;

import lombok.Builder;
import lombok.Value;

/**
 * Konfiguracja scrapera.
 * Używa Builder pattern do łatwego tworzenia obiektów.
 */
@Value
@Builder
public class ScraperConfig {
    /**
     * Ścieżka do katalogu z kodem źródłowym testów
     */
    String sourceDirectory;
    
    /**
     * Ścieżka wyjściowa dla pliku CSV
     */
    String outputFile;
    
    /**
     * Wzorzec nazw plików do przeszukania (np. *.java, *Test.java)
     */
    @Builder.Default
    String filePattern = "*.java";
    
    /**
     * Czy rekursywnie przeszukiwać podkatalogi
     */
    @Builder.Default
    boolean recursive = true;
    
    /**
     * Domyślna konfiguracja
     */
    public static ScraperConfig defaults() {
        return ScraperConfig.builder()
                .sourceDirectory("./src/test/java")
                .outputFile("./target/jira/allure-report.csv")
                .filePattern("*.java")
                .recursive(true)
                .build();
    }
}

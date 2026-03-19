package org.example.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.model.TestData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Generator raportów CSV z danych testowych.
 */
@Slf4j
public class CsvReportGenerator {

    private static final String[] HEADERS = {"className", "methodName", "issueKey", "description"};

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    /**
     * Generuje plik CSV z danych testowych.
     * Do nazwy pliku dodawany jest timestamp w formacie yyyyMMdd_HHmmss
     *
     * @param testDataList lista danych testowych
     * @param outputPath   ścieżka wyjściowa dla pliku CSV
     * @throws IOException błąd podczas zapisu pliku
     */
    public void generate(List<TestData> testDataList, String outputPath) throws IOException {
        // Dodaj timestamp do nazwy pliku
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        String fileNameWithTimestamp = addTimestampToFileName(outputPath, timestamp);
        
        log.info("Generowanie raportu CSV: {}", fileNameWithTimestamp);
        
        Path path = Path.of(fileNameWithTimestamp);
        
        // Utwórz katalog wyjściowy jeśli nie istnieje
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader(HEADERS)
                     .withTrim())) {
            
            for (TestData data : testDataList) {
                printer.printRecord(
                        data.getClassName(),
                        data.getMethodName(),
                        data.getIssueKey(),
                        escapeDescription(data.getDescription())
                );
            }
        }
        
        log.info("Wygenerowano raport z {} wierszami", testDataList.size());
    }

    /**
     * Dodaje timestamp do nazwy pliku.
     * Przykład: "report.csv" -> "report_20260319_104533.csv"
     */
    private String addTimestampToFileName(String filePath, String timestamp) {
        Path path = Path.of(filePath);
        String fileName = path.getFileName().toString();
        
        // Sprawdź czy plik ma rozszerzenie .csv
        if (fileName.toLowerCase().endsWith(".csv")) {
            int dotIndex = fileName.lastIndexOf('.');
            String name = fileName.substring(0, dotIndex);
            String extension = fileName.substring(dotIndex);
            return path.getParent() != null 
                    ? path.getParent().resolve(name + "_" + timestamp + extension).toString()
                    : name + "_" + timestamp + extension;
        } else {
            // Brak rozszerzenia .csv - dodaj timestamp na końcu
            return path.getParent() != null 
                    ? path.getParent().resolve(fileName + "_" + timestamp).toString()
                    : fileName + "_" + timestamp;
        }
    }

    /**
     * Zwraca liczbę wierszy w raporcie (bez nagłówka).
     */
    public int getRowCount(List<TestData> testDataList) {
        return testDataList.size();
    }

    /**
     * Escape'uje opis dla CSV - obsługuje znaki specjalne.
     */
    private String escapeDescription(String description) {
        if (description == null) {
            return "";
        }
        
        // Usuń białe znaki na początku i końcu
        String escaped = description.trim();
        
        // Zamień tabulacje na spacje
        escaped = escaped.replaceAll("\\t", " ");
        
        // Usuń wielokrotne spacje
        escaped = escaped.replaceAll(" +", " ");
        
        // Zamień znaki nowej linii na spacje (CSV obsługuje wielolinijkowe pola)
        // ale zachowaj strukturę dla czytelności
        escaped = escaped.replaceAll("\\r?\\n", " | ");
        
        return escaped;
    }
}

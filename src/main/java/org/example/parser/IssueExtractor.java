package org.example.parser;

import lombok.extern.slf4j.Slf4j;
import org.example.model.TestData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ekstraktor ticketów JIRA z tekstu.
 * Parsuje format: "ABCD-12345 - tytuł zadania" lub "ABCD-12345"
 */
@Slf4j
public class IssueExtractor {

    /**
     * Wzorzec dla numeru ticketu JIRA: ABCD-12345
     * Grupa 1: prefix (np. PROJ, ABCD)
     * Grupa 2: numer (np. 12345)
     */
    private static final Pattern JIRA_KEY_PATTERN = Pattern.compile("([A-Z]+)-(\\d+)");
    
    /**
     * Wzorzec dla ticketu z tytułem: ABCD-12345 - Tytuł
     */
    private static final Pattern JIRA_KEY_WITH_TITLE_PATTERN = Pattern.compile("([A-Z]+-\\d+)\\s*[-–—:]\\s*(.+?)(?=[A-Z]+-\\d+|\\s*$|$)");

    /**
     * Przetwarza listę TestData i wyciąga issue z description.
     * Jeśli issue jest null, szuka go w description.
     *
     * @param testDataList lista danych testowych
     * @return przetworzona lista z wyciągniętymi issue
     */
    public List<TestData> process(List<TestData> testDataList) {
        List<TestData> result = new ArrayList<>();
        
        for (TestData data : testDataList) {
            if (data.getIssueKey() == null && data.getDescription() != null) {
                // Wyciągnij issue z opisu
                List<String> issues = extractIssuesFromText(data.getDescription());
                
                if (issues.isEmpty()) {
                    // Brak issue w opisie - pomijamy
                    log.debug("Brak issue w opisie dla {}.{}", data.getClassName(), data.getMethodName());
                    continue;
                }
                
                // Dla każdego znalezionego issue tworzymy osobny wpis
                for (String issueKey : issues) {
                    result.add(new TestData(
                            data.getClassName(),
                            data.getMethodName(),
                            issueKey,
                            extractTitleForIssue(data.getDescription(), issueKey)
                    ));
                }
            } else {
                // Issue jest już podane bezpośrednio
                result.add(data);
            }
        }
        
        return result;
    }

    /**
     * Wyciąga wszystkie numery ticketów JIRA z tekstu.
     *
     * @param text tekst do przetworzenia
     * @return lista unikalnych numerów ticketów
     */
    public List<String> extractIssuesFromText(String text) {
        List<String> issues = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return issues;
        }
        
        // Znajdź wszystkie wystąpienia wzorca ABCD-12345
        Matcher matcher = JIRA_KEY_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String issueKey = matcher.group(1) + "-" + matcher.group(2);
            if (!issues.contains(issueKey)) {
                issues.add(issueKey);
            }
        }
        
        return issues;
    }

    /**
     * Wyciąga tytuł dla konkretnego issue z tekstu.
     *
     * @param text pełny tekst opisu
     * @param issueKey numer issue
     * @return tytuł issue lub null
     */
    public String extractTitleForIssue(String text, String issueKey) {
        if (text == null || issueKey == null) {
            return null;
        }
        
        // Szukaj wzorca: ISSUE_KEY - tytuł
        // Używamy lookbehind aby znaleźć wszystkie wystąpienia
        Pattern pattern = Pattern.compile(
                Pattern.quote(issueKey) + "\\s*[-–—:]\\s*([^\\n]+?)(?=[A-Z]+-\\d+|\\s*$)",
                Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            return title.isEmpty() ? null : title;
        }
        
        return null;
    }

    /**
     * Przetwarza pojedynczy opis i zwraca mapę issue -> tytuł.
     *
     * @param description tekst opisu
     * @return mapa issue -> tytuł
     */
    public java.util.Map<String, String> extractIssueTitleMap(String description) {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        
        if (description == null || description.isEmpty()) {
            return result;
        }
        
        Matcher matcher = JIRA_KEY_WITH_TITLE_PATTERN.matcher(description);
        
        while (matcher.find()) {
            String issueKey = matcher.group(1);
            String title = matcher.group(2).trim();
            result.put(issueKey, title);
        }
        
        return result;
    }
}

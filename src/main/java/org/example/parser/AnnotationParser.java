package org.example.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.PackageDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.example.model.TestData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser adnotacji Allure z plików źródłowych Java.
 * Wyciąga @Issue i @Description z metod testowych.
 */
@Slf4j
public class AnnotationParser {

    private static final String ISSUE_ANNOTATION = "TmsLink";
    private static final String DESCRIPTION_ANNOTATION = "Description";

    /**
     * Parsuje plik źródłowy Java i zwraca listę danych testowych.
     *
     * @param filePath ścieżka do pliku .java
     * @return lista obiektów TestData z adnotacji
     */
    public List<TestData> parse(Path filePath) {
        List<TestData> testDataList = new ArrayList<>();
        
        try {
            // Konfiguracja dla obsługi Java 21
            ParserConfiguration config = new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            
            String sourceCode = Files.readString(filePath);
            CompilationUnit cu = new com.github.javaparser.JavaParser(config).parse(sourceCode).getResult().orElseThrow();
            
            // Znajdź wszystkie klasy w pliku
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                String className = classDecl.getNameAsString();
                String packageName = cu.getPackageDeclaration()
                        .map(PackageDeclaration::getNameAsString)
                        .orElse("");
                String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
                
                log.debug("Przetwarzanie klasy: {}", fullClassName);
                
                // Znajdź wszystkie metody w klasie
                classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                    List<TestData> methodData = extractTestData(fullClassName, method);
                    testDataList.addAll(methodData);
                });
            });
            
        } catch (IOException e) {
            log.error("Błąd podczas parsowania pliku: {}", filePath, e);
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas parsowania: {}", filePath, e);
        }
        
        return testDataList;
    }

    /**
     * Wyciąga dane z adnotacji metody.
     */
    private List<TestData> extractTestData(String className, MethodDeclaration method) {
        List<TestData> result = new ArrayList<>();
        
        String methodName = method.getNameAsString();
        
        // Znajdź adnotację @Issue
        List<String> issues = extractIssues(method);
        
        // Znajdź adnotację @Description
        String description = extractDescription(method);
        
        // Jeśli nie ma issue, sprawdź czy jest w opisie
        if (issues.isEmpty() && description != null) {
            // Issue zostaną wyciągnięte z description później przez IssueExtractor
            issues.add(null); // Placeholder dla "issue z description"
        }
        
        // Jeśli nadal nie ma żadnego issue, pomijamy metodę
        if (issues.isEmpty()) {
            return result;
        }
        
        // Utwórz obiekt TestData dla każdego issue
        for (String issue : issues) {
            result.add(new TestData(className, methodName, issue, description));
        }

        return result;
    }

    /**
     * Wyciąga wartości z adnotacji @Issue.
     */
    private List<String> extractIssues(MethodDeclaration method) {
        List<String> issues = new ArrayList<>();
        
        // Szukaj adnotacji @Issue
        List<AnnotationExpr> issueAnnotations = method.getAnnotations().stream()
                .filter(a -> {
                    String name = a.getNameAsString();
                    return name.equals(ISSUE_ANNOTATION) || 
                           name.endsWith("." + ISSUE_ANNOTATION);
                })
                .toList();
        
        for (AnnotationExpr annotation : issueAnnotations) {
            Optional<String> value = extractAnnotationValue(annotation);
            value.ifPresent(issues::add);
        }
        
        return issues;
    }

    /**
     * Wyciąga wartość z adnotacji @Description.
     */
    private String extractDescription(MethodDeclaration method) {
        Optional<AnnotationExpr> descAnnotation = method.getAnnotations().stream()
                .filter(a -> {
                    String name = a.getNameAsString();
                    return name.equals(DESCRIPTION_ANNOTATION) || 
                           name.endsWith("." + DESCRIPTION_ANNOTATION);
                })
                .findFirst();
        
        return descAnnotation
                .flatMap(this::extractAnnotationValue)
                .orElse(null);
    }

    /**
     * Wyciąga wartość z adnotacji (obsługuje różne formaty).
     */
    private Optional<String> extractAnnotationValue(AnnotationExpr annotation) {
        // Próbuj znaleźć StringLiteralExpr w adnotacji
        Optional<StringLiteralExpr> stringLiteral = annotation.findFirst(StringLiteralExpr.class);
        if (stringLiteral.isPresent()) {
            return Optional.of(stringLiteral.get().getValue());
        }
        
        // Obsługa TextBlockLiteralExpr (Java 15+)
        Optional<com.github.javaparser.ast.expr.TextBlockLiteralExpr> textBlock = 
                annotation.findFirst(com.github.javaparser.ast.expr.TextBlockLiteralExpr.class);
        if (textBlock.isPresent()) {
            return Optional.of(textBlock.get().getValue());
        }
        
        return Optional.empty();
    }
}

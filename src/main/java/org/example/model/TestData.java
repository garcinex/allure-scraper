package org.example.model;

import lombok.Value;

/**
 * Reprezentacja pojedynczego testu z extracted danymi z adnotacji Allure.
 * Immutable class - wszystkie pola są final.
 */
@Value
public class TestData {
    /**
     * Pełna nazwa klasy testowej (package + nazwa klasy)
     */
    String className;
    
    /**
     * Nazwa metody testowej
     */
    String methodName;
    
    /**
     * Numer ticketu JIRA (np. PROJ-1234)
     */
    String issueKey;
    
    /**
     * Opis testu z adnotacji @Description
     */
    String description;
}

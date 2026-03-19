package com.example.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.Test;

/**
 * Przykładowe testy z adnotacjami Allure.
 */
public class LoginTest {

    @Test
    @Issue("PROJ-1001")
    @Description("Test poprawnego logowania do systemu")
    void shouldLoginWithValidCredentials() {
        // test implementation
    }

    @Test
    @Issue("PROJ-1002")
    @Description("Test odrzucenia nieprawidłowego hasła")
    void shouldRejectInvalidPassword() {
        // test implementation
    }

    @Test
    @Issue("PROJ-1002")
    @Issue("PROJ-1003")
    @Description("""
    PROJ-1002 - Test odrzucenia nieprawidłowego hasła
    PROJ-1003 - Test odrzucenia nieprawidłowego loginu""")
    void shouldRejectInvalidLogin() {
        // test implementation
    }

    @Test
    @Issue("PROJ-1003")
    @Issue("PROJ-1004")
    @Description("Test z wieloma issue")
    void shouldHandleMultipleIssues() {
        // test implementation
    }

    @Test
    @Description("""
        PROJ-2001 - Weryfikacja adresu email
        PROJ-2012 - Sprawdzenie formatu numeru telefonu
        """)
    void shouldValidateUserData() {
        // test implementation
    }
}

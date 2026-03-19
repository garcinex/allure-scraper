package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.cli.ScraperCommand;
import org.example.model.ScraperConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loader konfiguracji z plików YAML i argumentów CLI.
 * Priorytet: CLI args (tylko jawnie podane) > plik config > wartości domyślne
 */
@Slf4j
public class ConfigLoader {

    /**
     * Ładuje konfigurację z pliku i opcjonalnie łączy z CLI args.
     *
     * @param configFilePath ścieżka do pliku konfiguracyjnego (może być null)
     * @param command obiekt ScraperCommand z argumentami CLI
     * @return finalna konfiguracja
     */
    public ScraperConfig load(String configFilePath, ScraperCommand command) {
        log.info("Ładowanie konfiguracji...");

        // Startuj od wartości domyślnych
        ScraperConfig config = ScraperConfig.defaults();

        // Użyj jawnie podanego pliku konfiguracyjnego lub szukaj domyślnego
        String configPathToLoad = configFilePath;
        
        // Jeśli nie podano pliku konfiguracyjnego, szukaj domyślnych
        if (configPathToLoad == null || configPathToLoad.isEmpty()) {
            // Sprawdź domyślne nazwy plików konfiguracyjnych
            String[] defaultConfigs = {"config.yaml", "config.yml", "config.properties"};
            for (String defaultConfig : defaultConfigs) {
                Path defaultPath = Path.of(defaultConfig);
                if (Files.exists(defaultPath)) {
                    configPathToLoad = defaultConfig;
                    log.debug("Znaleziono domyślny plik konfiguracyjny: {}", defaultConfig);
                    break;
                }
            }
        }

        // Nadpisz z pliku konfiguracyjnego
        if (configPathToLoad != null && !configPathToLoad.isEmpty()) {
            Path path = Path.of(configPathToLoad);
            if (Files.exists(path)) {
                config = loadFromYaml(path, config);
                log.debug("Załadowano konfigurację z pliku: {}", configPathToLoad);
            } else {
                log.warn("Plik konfiguracyjny nie istnieje: {}", configPathToLoad);
            }
        }

        // Nadpisz z CLI args (tylko jawnie podane wartości, nie domyślne)
        if (command != null) {
            config = mergeCliConfig(config, command);
        }

        log.info("Konfiguracja załadowana: {}", config);
        return config;
    }

    /**
     * Ładuje konfigurację z pliku YAML.
     */
    private ScraperConfig loadFromYaml(Path path, ScraperConfig current) {
        log.debug("Ładowanie konfiguracji z pliku YAML: {}", path);
        Yaml yaml = new Yaml();
        
        try (InputStream is = Files.newInputStream(path)) {
            Map<String, Object> data = yaml.load(is);
            if (data == null) {
                return current;
            }

            String sourceDir = getStringValue(data, "sourceDirectory", current.getSourceDirectory());
            String outputFile = getStringValue(data, "outputFile", current.getOutputFile());
            String filePattern = getStringValue(data, "filePattern", current.getFilePattern());
            boolean recursive = getBooleanValue(data, "recursive", current.isRecursive());

            return ScraperConfig.builder()
                    .sourceDirectory(sourceDir)
                    .outputFile(outputFile)
                    .filePattern(filePattern)
                    .recursive(recursive)
                    .build();
        } catch (Exception e) {
            log.error("Błąd podczas ładowania pliku konfiguracyjnego: {}", path, e);
            return current;
        }
    }

    /**
     * Pobiera wartość String z mapy YAML.
     */
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Pobiera wartość boolean z mapy YAML.
     */
    private boolean getBooleanValue(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * Łączy konfigurację z CLI - tylko jawnie podane wartości nadpisują plik konfiguracyjny.
     */
    private ScraperConfig mergeCliConfig(ScraperConfig current, ScraperCommand command) {
        // Tylko jawnie podane wartości CLI nadpisują konfigurację z pliku
        // (nie domyślne wartości)
        
        String sourceDir = current.getSourceDirectory();
        String outputFile = current.getOutputFile();
        String filePattern = current.getFilePattern();
        boolean recursive = current.isRecursive();

        if (command.isSourceDirectorySet()) {
            sourceDir = command.getSourceDirectory();
            log.debug("CLI nadpisuje sourceDirectory: {}", sourceDir);
        }
        
        if (command.isOutputFileSet()) {
            outputFile = command.getOutputFile();
            log.debug("CLI nadpisuje outputFile: {}", outputFile);
        }
        
        if (command.isFilePatternSet()) {
            filePattern = command.getFilePattern();
            log.debug("CLI nadpisuje filePattern: {}", filePattern);
        }
        
        if (command.isRecursiveSet()) {
            recursive = command.getRecursive();
            log.debug("CLI nadpisuje recursive: {}", recursive);
        }

        return ScraperConfig.builder()
                .sourceDirectory(sourceDir)
                .outputFile(outputFile)
                .filePattern(filePattern)
                .recursive(recursive)
                .build();
    }
}

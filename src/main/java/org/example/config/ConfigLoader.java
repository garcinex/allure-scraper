package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ScraperConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loader konfiguracji z plików YAML i argumentów CLI.
 * Priorytet: CLI args > plik config > wartości domyślne
 */
@Slf4j
public class ConfigLoader {

    /**
     * Ładuje konfigurację z pliku i opcjonalnie łączy z CLI args.
     *
     * @param configFilePath ścieżka do pliku konfiguracyjnego (może być null)
     * @param cliConfig konfiguracja z CLI (może być null)
     * @return finalna konfiguracja
     */
    public ScraperConfig load(String configFilePath, ScraperConfig cliConfig) {
        log.info("Ładowanie konfiguracji...");

        // Startuj od wartości domyślnych
        ScraperConfig config = ScraperConfig.defaults();

        // Nadpisz z pliku konfiguracyjnego (jeśli istnieje)
        if (configFilePath != null && !configFilePath.isEmpty()) {
            Path path = Path.of(configFilePath);
            if (Files.exists(path)) {
                config = loadFromYaml(path, config);
            } else {
                log.warn("Plik konfiguracyjny nie istnieje: {}", configFilePath);
            }
        }

        // Nadpisz z CLI args (najwyższy priorytet)
        if (cliConfig != null) {
            config = mergeCliConfig(cliConfig);
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
     * Łączy konfigurację z CLI - wartości z CLI zawsze mają pierwszeństwo.
     */
    private ScraperConfig mergeCliConfig(ScraperConfig cli) {
        // CLI values always take precedence over config file values
        return ScraperConfig.builder()
                .sourceDirectory(cli.getSourceDirectory())
                .outputFile(cli.getOutputFile())
                .filePattern(cli.getFilePattern())
                .recursive(cli.isRecursive())
                .build();
    }
}

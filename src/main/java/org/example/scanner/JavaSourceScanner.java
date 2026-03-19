package org.example.scanner;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ScraperConfig;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skaner plików źródłowych Java w katalogu.
 */
@Slf4j
public class JavaSourceScanner {

    private final ScraperConfig config;

    public JavaSourceScanner(ScraperConfig config) {
        this.config = config;
    }

    /**
     * Skanuje katalog i zwraca listę plików .java spełniających kryteria.
     *
     * @return lista ścieżek do plików Java
     */
    public List<Path> scan() throws IOException {
        Path sourceDir = Paths.get(config.getSourceDirectory());
        
        if (!Files.exists(sourceDir)) {
            log.error("Katalog źródłowy nie istnieje: {}", sourceDir);
            throw new IllegalArgumentException("Katalog źródłowy nie istnieje: " + sourceDir);
        }

        if (!Files.isDirectory(sourceDir)) {
            log.error("Ścieżka źródłowa nie jest katalogiem: {}", sourceDir);
            throw new IllegalArgumentException("Ścieżka źródłowa nie jest katalogiem: " + sourceDir);
        }

        log.info("Skanowanie katalogu: {}", sourceDir);

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + config.getFilePattern());

        EnumSet<FileVisitOption> options = config.isRecursive() 
                ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) 
                : EnumSet.noneOf(FileVisitOption.class);

        List<Path> javaFiles = new ArrayList<>();
        
        Files.walkFileTree(sourceDir, options, Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (matcher.matches(file.getFileName())) {
                    log.debug("Znaleziono plik: {}", file);
                    javaFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.warn("Nie udało się odwiedzić pliku: {}", file, exc);
                return FileVisitResult.CONTINUE;
            }
        });

        log.info("Znaleziono {} plików Java", javaFiles.size());
        return javaFiles;
    }

    /**
     * Zwraca listę plików jako lista ścieżek string.
     */
    public List<String> scanAsStrings() throws IOException {
        return scan().stream()
                .map(Path::toString)
                .collect(Collectors.toList());
    }
}

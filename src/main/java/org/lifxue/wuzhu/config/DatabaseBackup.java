package org.lifxue.wuzhu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Performs automatic database backup before migrations run.
 * Creates a timestamped backup of the H2 database files.
 */
@Slf4j
@Component
public class DatabaseBackup {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @EventListener(ApplicationStartedEvent.class)
    public void backupBeforeMigration() {
        log.info("Starting database backup before migration...");

        try {
            Path dbPath = extractDatabasePath();
            if (dbPath == null || !Files.exists(dbPath)) {
                log.info("Database directory does not exist yet, skipping backup.");
                return;
            }

            Path backupDir = dbPath.getParent().resolve("backups");
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            Path backupPath = backupDir.resolve("wuzhudbjpa_backup_" + timestamp);

            backupDirectory(dbPath, backupPath);
            log.info("Database backup completed: {}", backupPath);

            cleanupOldBackups(backupDir);

        } catch (Exception e) {
            log.error("Database backup failed: {}", e.getMessage(), e);
        }
    }

    private Path extractDatabasePath() {
        try {
            if (datasourceUrl != null && datasourceUrl.startsWith("jdbc:h2:")) {
                String path = datasourceUrl.substring(8);
                int semicolonIndex = path.indexOf(';');
                if (semicolonIndex > 0) {
                    path = path.substring(0, semicolonIndex);
                }

                if (path.startsWith("~")) {
                    String homeDir = System.getProperty("user.home");
                    path = homeDir + path.substring(1);
                }

                Path dbFile = Paths.get(path);
                return dbFile.getParent();
            }
        } catch (Exception e) {
            log.error("Failed to parse database URL: {}", e.getMessage());
        }
        return null;
    }

    private void backupDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path relativePath = source.relativize(sourcePath);
                Path targetPath = target.resolve(relativePath);

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                log.error("Failed to backup file {}: {}", sourcePath, e.getMessage());
            }
        });
    }

    private void cleanupOldBackups(Path backupDir) {
        try {
            int maxBackups = 5;

            Files.list(backupDir)
                    .filter(Files::isDirectory)
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .skip(maxBackups)
                    .forEach(oldBackup -> {
                        try {
                            deleteDirectory(oldBackup);
                            log.info("Cleaned up old backup: {}", oldBackup);
                        } catch (IOException e) {
                            log.error("Failed to delete old backup {}: {}", oldBackup, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to cleanup old backups: {}", e.getMessage());
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("Failed to delete {}: {}", path, e.getMessage());
                    }
                });
    }
}

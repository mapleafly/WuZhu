package org.lifxue.wuzhu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 数据库备份服务
 * 提供数据导出和导入功能
 *
 * @author Sisyphus
 */
@Slf4j
@Service
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseBackupService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 导出数据库到SQL文件
     *
     * @param backupDir 备份目录
     * @return 备份文件路径
     */
    public String exportToSql(String backupDir) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "wuzhu_backup_" + timestamp + ".sql";
            Path backupPath = Paths.get(backupDir, fileName);

            // 确保备份目录存在
            Files.createDirectories(backupPath.getParent());

            // 使用H2的SCRIPT命令导出
            jdbcTemplate.execute("SCRIPT TO '" + backupPath.toAbsolutePath().toString().replace("\\", "/") + "'");

            // 过滤掉Flyway系统表相关的SQL语句
            filterFlywayStatements(backupPath);

            log.info("数据库备份成功: {}", backupPath);
            return backupPath.toString();
        } catch (Exception e) {
            log.error("数据库备份失败", e);
            throw new RuntimeException("备份失败: " + e.getMessage(), e);
        }
    }

    /**
     * 过滤SQL文件中的Flyway系统表相关语句
     *
     * @param sqlFilePath SQL文件路径
     */
    private void filterFlywayStatements(Path sqlFilePath) throws IOException {
        String content = Files.readString(sqlFilePath);

        // 移除所有包含FLYWAY_SCHEMA_HISTORY的行（不区分大小写）
        String[] lines = content.split("\n");
        StringBuilder filtered = new StringBuilder();

        for (String line : lines) {
            // 跳过包含flyway_schema_history的行
            if (!line.toUpperCase().contains("FLYWAY_SCHEMA_HISTORY")) {
                filtered.append(line).append("\n");
            }
        }

        Files.writeString(sqlFilePath, filtered.toString());
    }

    /**
     * 从SQL文件导入数据
     *
     * @param sqlFilePath SQL文件路径
     */
    public void importFromSql(String sqlFilePath) {
        try {
            File sqlFile = new File(sqlFilePath);
            if (!sqlFile.exists()) {
                throw new RuntimeException("备份文件不存在: " + sqlFilePath);
            }

            // 先删除所有现有表和数据，然后导入（不带DELETE FILES，保留数据库文件）
            jdbcTemplate.execute("DROP ALL OBJECTS");
            
            // 使用H2的RUNSCRIPT命令导入
            jdbcTemplate.execute("RUNSCRIPT FROM '" + sqlFile.getAbsolutePath().replace("\\", "/") + "'");

            log.info("数据库恢复成功: {}", sqlFilePath);
        } catch (Exception e) {
            log.error("数据库恢复失败", e);
            throw new RuntimeException("恢复失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取默认备份目录
     *
     * @return 备份目录路径
     */
    public String getDefaultBackupDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".wuzhu", "backups").toString();
    }

    /**
     * 备份数据库到ZIP文件
     *
     * @param backupDir 备份目录
     * @return 备份文件路径
     */
    public String backupToZip(String backupDir) {
        String sqlFile = null;
        try {
            // 先导出SQL
            sqlFile = exportToSql(backupDir);

            // 创建ZIP
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String zipFileName = "wuzhu_backup_" + timestamp + ".zip";
            Path zipPath = Paths.get(backupDir, zipFileName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                File fileToZip = new File(sqlFile);
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                }
            }

            // 删除原始SQL文件
            Files.deleteIfExists(Paths.get(sqlFile));

            log.info("数据库ZIP备份成功: {}", zipPath);
            return zipPath.toString();
        } catch (Exception e) {
            log.error("ZIP备份失败", e);
            throw new RuntimeException("ZIP备份失败: " + e.getMessage(), e);
        }
    }
}

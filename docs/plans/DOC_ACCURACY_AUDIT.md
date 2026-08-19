# WuZhu 文档与代码一致性审查报告

> 本报告基于 `pom.xml`（当前 HEAD `393fff1`）及 `src/` 源码，逐一核对 `README.md`、`README_CN.md`、`AGENTS.md` 及 `packaging/` 下各文档的描述是否与代码一致。
> 生成时间：2025-08-19 ｜ 分析对象：master 分支最新提交

---

## 一、结论速览

| 文档 | 总体结论 |
|------|---------|
| `README.md` / `README_CN.md` | ❌ 技术栈版本信息**严重过时**（仍显示 Spring Boot 2.7.10 时代信息），需更新 |
| `AGENTS.md` | ⚠️ 存在**两份互相矛盾的技术栈表**（新/旧各一份），且 Flyway 版本、ddl-auto 描述有误 |
| `packaging/DEVELOPMENT.md` | ⚠️ 调试命令有拼写错误，其余基本正确 |
| `packaging/PACKAGING_UBUNTU.md` | ⚠️ jpackage `--main-class` 路径过时（Boot 3.2 下会运行失败） |
| `packaging/PACKAGING_WINDOWS.md` | ⚠️ 同上 `--main-class` 路径过时 |
| `packaging/BUILD_GUIDE.md` | ⚠️ 同上 |
| `packaging/package-ubuntu.sh` / `package-windows.ps1` | ⚠️ 同上 `--main-class` 路径过时 |
| `packaging/README.md` | ⚠️ CI/CD 章节是"建议"但仓库内没有 `.github/workflows` 实际文件 |

---

## 二、按文档逐项核对

### 2.1 README.md 与 README_CN.md

两个文件内容结构一致（仅中英文差异），以下核对项对两者同时成立。

#### ❌ 错误/过时项

| 文档描述 | 文档位置 | `pom.xml` 实际值 | 结论 |
|---------|---------|-----------------|------|
| Spring Boot **2.7.10** | README badge + 技术栈表 | **3.2.0**（`spring-boot-starter-parent`） | ❌ 过时 |
| OpenFeign **2021.0.3** | 技术栈表 | **2023.0.0**（spring-cloud-dependencies） | ❌ 过时 |
| H2 **2.2.220** | 技术栈表 | **2.2.224** | ❌ 过时 |
| MapStruct **1.5.5** | 技术栈表 | **1.6.2** | ❌ 过时 |
| README badge "Spring Boot 2.7.10" | 顶部 shields.io | 3.2.0 | ❌ 过时 |

> 说明：本项目曾从 Spring Boot 2.7.10 升级到 3.2.0（见 `UPGRADE_REPORT.md`），但 README 未同步更新。

#### ✅ 正确项

| 文档描述 | 文档位置 | `pom.xml` 实际值 | 结论 |
|---------|---------|-----------------|------|
| Java 21 | 技术栈表 | `java.version=21` | ✅ |
| JavaFX 21.0.2 | 技术栈表 | `javafx.version=21.0.2` | ✅ |
| WorkbenchFX 11.3.1 | 技术栈表 | `workbenchfx.version=11.3.1` | ✅ |
| Lombok 1.18.30 | 技术栈表 | `1.18.30` | ✅ |
| RichTextFX 0.11.0 | 技术栈表 | `0.11.0` | ✅ |
| Maven 3.8+（wrapper） | 环境要求 | wrapper 为 **3.8.6** | ✅ |
| JAR 名 `WuZhu-1.0.jar` | 构建命令 | artifactId=`WuZhu`, version=`1.0` | ✅ |
| 8 个业务模块 | 项目结构 | `modules/` 下确有 8 个包（cash/file/note/piechart/selectcoin/setting/statistics/tradeinfo） | ✅（注：file 为导入导出工具类，非视图模块） |
| 启动流程 | 应用架构 | 与 `WuZhuApplication`/`JavaFxApplication`/`PrimaryStageInitializer` 一致 | ✅ |
| H2 文件位置 `~/.wuzhu/h2/wuzhudbjpa` | 数据库 | `application.yml` 一致 | ✅ |
| API 端点/认证头 | API 集成 | 与 `application.yml` 一致 | ✅ |

### 2.2 AGENTS.md

#### ❌ 错误项

| 文档描述 | 位置 | 实际值 | 结论 |
|---------|------|--------|------|
| **两份技术栈表** | 第 15-24 行与第 51-63 行 | 第一份为 Boot 3.2 时代（正确），第二份为 Boot 2.7.10 时代（旧、废弃） | ❌ 内容重复且自相矛盾，需删除旧表 |
| Flyway **10.x** | 第一份技术栈表 | Spring Boot 3.2.0 托管 **9.22.3**（见 BOM 的 `flyway.version`） | ❌ 应为 9.22.3 |
| `ddl-auto: update` | "配置说明-数据库" | `application.yml` 为 **`ddl-auto: validate`**（Flyway 负责结构） | ❌ 应为 validate |
| "已添加 jpackage 插件" | 相关文件 | pom 中**确有** `org.panteleyev:jpackage-maven-plugin:1.4.0`，但配置为 `module` 方式且**未绑定执行阶段**，与项目实际"fat jar"打包方式不符（详见 §3） | ⚠️ 表述含糊，实际插件配置基本不可用 |

#### ✅ 正确项

| 文档描述 | 结论 |
|---------|------|
| Java 21 / JavaFX 21.0.2 / WorkbenchFX 11.3.1 | ✅ |
| OpenFeign 2023.0.0 / MapStruct 1.6.2（第一份表） | ✅ |
| `web-application-type: none`（非 Web 应用） | ✅（`application.yml` 一致） |
| "仅有一个 Spring 上下文测试" | ✅（`src/test/.../WuZhuApplicationTests.java`） |
| 启动流程 | ✅ |
| 8 个模块、目录结构 | ✅ |

> ⚠️ AGENTS.md 头部 "生成时间 2025-06-03 / 提交 f44661f"，但当前 HEAD 是 `393fff1`，已落后多个提交。

### 2.3 packaging/DEVELOPMENT.md

| 项 | 结论 |
|----|------|
| 环境要求（JDK 21 / Maven 3.8+ / Git） | ✅ |
| 三种运行方式（`spring-boot:run` / `java -jar` / `javafx:run`） | ✅ |
| **调试命令拼写错误**：`-agentlib:jdlib=` 应为 `-agentlib:jdwp=` | ❌（第 102 行） |
| 配置优先级说明 | ✅ |
| `application-dev.yml` 建议 | ✅（仅供参考，仓库内无此文件） |

### 2.4 packaging/PACKAGING_UBUNTU.md / PACKAGING_WINDOWS.md / BUILD_GUIDE.md

#### ❌ 关键错误：`--main-class` 指向不存在的类

- 文档与脚本中统一使用：
  ```bash
  --main-class org.springframework.boot.loader.JarLauncher
  ```
- 该路径是 **Spring Boot 2.x** 的 JarLauncher 位置。本项目使用 **Spring Boot 3.2.0**，`spring-boot-loader-3.2.0.jar` 中 JarLauncher 的真实位置为：
  ```
  org.springframework.boot.loader.launch.JarLauncher
  ```
  旧路径 `org.springframework.boot.loader.JarLauncher` **已不存在**（已验证 3.2.0 jar 内容）。
- **后果**：按文档/脚本生成的安装包，启动时会因找不到主类而失败（`ClassNotFoundException`）。
- **修复**：所有 `--main-class org.springframework.boot.loader.JarLauncher` 改为 `--main-class org.springframework.boot.loader.launch.JarLauncher`。

涉及文件：
- `packaging/PACKAGING_UBUNTU.md`（第 125、271 行）
- `packaging/PACKAGING_WINDOWS.md`（第 120、290 行）
- `packaging/BUILD_GUIDE.md`（第 74、89 行等）
- `packaging/package-ubuntu.sh`（第 97 行）
- `packaging/package-windows.ps1`（第 112 行）

> ⚠️ 另一个隐患：Boot 3.2 的 `JarLauncher` 需要从 fat jar 内加载嵌套 jar。若采用 `--input target/dependency` + 把 fat jar 也放进去的"复制依赖"方式，会让 fat jar 与外部依赖同时出现在 classpath，可能产生类加载冲突。更稳妥的 jpackage 用法见 `docs/plans/WINDOWS_PACKAGING_WSL2.md`。

#### 其他小问题

- `PACKAGING_UBUNTU.md` 第 60 行：下载链接写死 `21.0.7+10`，实际 BellSoft 当前为 `21.0.7+9` 及更高版本；建议改为"从 https://bell-sw.com/pages/downloads 获取最新版"。
- `PACKAGING_UBUNTU.md` 第 83 行注释：`--print-module-path` 并非只有 BellSoft/Temurin 可用（标准 OpenJDK 也有），属表述误差。
- `PACKAGING_UBUNTU.md` 第 22 行：`sudo apt install openjdk-21-jdk`（Ubuntu 24.04 默认仓库可能没有 openjdk-21，只有 openjdk-21-jdk-headless 或需 PPA）。用 mise 方案可完全绕开（见 `DEV_ENV_MISE.md`）。

### 2.5 packaging/package-ubuntu.sh 与 package-windows.ps1

- 逻辑基本正确，均包含环境检查、mvnw 构建、jlink/jpackage。
- ❌ 两者都用旧 `--main-class org.springframework.boot.loader.JarLauncher`（同上）。
- ⚠️ `package-ubuntu.sh` 第 79 行使用 `java --print-module-path` 拼 jlink module-path——若 JDK 由 mise 安装的 Liberica Full 提供，此方式可用；但更稳妥是直接用 `$JAVA_HOME/jmods`。
- ⚠️ `package-windows.ps1` 中 `--app-version 1.0.0` 与 pom `version=1.0` 不一致（jpackage 营销版本号），建议发布流程中统一为从 git tag 推导（见 `RELEASE_AUTOMATION.md`）。

### 2.6 packaging/README.md

- 文档索引、快速导航正确。
- ⚠️ "自动化 CI/CD" 章节展示了一份建议的 `.github/workflows/build.yml`，但**仓库内并不存在 `.github/` 目录**（已验证）。该章节是"推荐做法"，尚未落地。
- ⚠️ 该建议 workflow 中 `actions/setup-java@v4` 使用了 `javafx: true` 参数——**该参数在 setup-java 中不存在**（实际为 `java-package: jdk+fx`，见 `docs/plans/DEV_ENV_MISE.md` §4.3）。需修正。

---

## 三、pom.xml 内部问题（与文档无关，但与打包方案强相关）

1. **jpackage-maven-plugin 配置基本不可用**：
   ```xml
   <plugin>
     <groupId>org.panteleyev</groupId>
     <artifactId>jpackage-maven-plugin</artifactId>
     <version>1.4.0</version>
     <configuration>
       ...
       <module>org.lifxue.wuzhu/org.lifxue.wuzhu.WuZhuApplication</module>
       <runtimeImage>target/image</runtimeImage>
     </configuration>
   </plugin>
   ```
   - `<module>` 方式要求项目是 **JPMS 模块化**（有 `module-info.java`），本项目**没有** `module-info.java`，是普通 Spring Boot fat jar。
   - 插件未配置 `<executions>` 绑定阶段，`mvn package` 时不会真正执行。
   - 结论：这段配置既不会生效、也方向错误。建议删除或改造成 `mainJar` + `mainClass` 的 fat-jar 方式（且版本升级到 1.7.4+）。
2. **Flyway 版本**：`pom.xml` 未显式声明 flyway 版本，由 Boot 3.2.0 BOM 托管为 **9.22.3**。AGENTS.md 说 "10.x" 是错的。
3. **maven-compiler-plugin 3.11.0 + antrun 替换 javax→jakarta**：能工作（`UPGRADE_REPORT.md` 说明了原因），但 MapStruct 1.6.2 生成 `javax.annotation.processing.Generated` 被替换为 `jakarta.annotation.Generated` 的方式属于 hack，升级到 MapStruct 1.7.x 可去除该 antrun hack。
4. **依赖冗余**：`spring-boot-devtools`（runtime, optional）在生产打包会引入；`httpclient` 4.5.14 与 Spring Cloud 的 `feign-okhttp` 并存（`application.yml` 中 `feign.httpclient.enabled: false`、`okhttp.enabled: true`），httpclient 依赖其实未使用。

---

## 四、文档修复优先级建议

| 优先级 | 事项 | 涉及文件 |
|-------|------|---------|
| 🔴 P0 | 所有 jpackage `--main-class` 改为 `org.springframework.boot.loader.launch.JarLauncher` | package-ubuntu.sh, package-windows.ps1, PACKAGING_UBUNTU.md, PACKAGING_WINDOWS.md, BUILD_GUIDE.md |
| 🔴 P0 | 修复 AGENTS.md 重复且矛盾的技术栈表（删除旧表），Flyway 9.22.3，ddl-auto validate | AGENTS.md |
| 🟠 P1 | 更新 README/README_CN 技术栈版本（Spring Boot 3.2.0, OpenFeign 2023.0.0, H2 2.2.224, MapStruct 1.6.2） | README.md, README_CN.md |
| 🟠 P1 | 修正 DEVELOPMENT.md 调试命令 `jdwp` 拼写 | packaging/DEVELOPMENT.md |
| 🟡 P2 | pom.xml jpackage 插件改造或删除（配合新打包方案） | pom.xml |
| 🟡 P2 | packaging/README.md CI/CD 章节用 setup-java 正确参数（`java-package: jdk+fx`）并落地 `.github/workflows` | packaging/README.md（+ 新建 workflow） |
| 🟢 P3 | Liberica 下载链接改为"最新版"而非写死版本号 | PACKAGING_UBUNTU.md |

> 以上仅列出问题与修复建议；具体是否修改、何时修改，由你确认后另行执行。

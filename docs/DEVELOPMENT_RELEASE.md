# WuZhu 开发与发布流程

> 本文档定义 WuZhu 项目的**日常开发流程**与**版本发布流程**，供人工或 Agent 遵循，按步骤自动发布新版本。
> 所有命令均已在 **WSL2 + Ubuntu 24.04 + GitHub Actions** 环境实测验证（v1.0.1 → v1.0.4，含 Windows `failed to launch JVM` 完整排障）。

---

## 一、开发流程

### 1.1 环境准备（mise）

项目使用 [mise](https://mise.jdx.dev) 统一管理 JDK 与 Maven，仓库根目录已包含 [`.mise.toml`](../.mise.toml)：

```toml
[tools]
java = "liberica-javafx-21.0.12+10"   # BellSoft Liberica JDK 21 Full（含 JavaFX 模块）
```

一次性初始化：

```bash
# 1. 激活 mise（写入 ~/.bashrc 或 ~/.zshrc，并重新登录 shell）
eval "$(mise activate bash)"          # bash
# eval "$(mise activate zsh)"         # zsh

# 2. 安装项目所需工具（读取 .mise.toml）
cd /path/to/WuZhu
mise install

# 3. 全局安装 Maven（推荐）
mise use -g maven@3.9.16

# 4. 验证
java -version              # 21.0.x
java --list-modules | grep javafx   # 应有 javafx.controls 等
mvn -version               # Apache Maven 3.9.x
jpackage --version         # 21.0.x
```

> ⚠️ 必须使用 **Liberica JDK 21 Full（含 JavaFX）**，普通 OpenJDK 不含 `javafx.*` 模块，运行/打包都会失败。CI 中通过 `actions/setup-java` 的 `java-package: jdk+fx` 安装（注意：**不存在** `javafx: true` 参数）。

### 1.2 分支策略

```
master（长期稳定分支，始终可构建）
   └── feature/xxx  或  fix/xxx / release/x.y.z（开发分支，完成后合回 master）
```

- 新功能 / 修复 / 发版准备，一律在**新分支**上开发，本地验证通过后 **`--no-ff` 合并**到 `master`。
- 禁止直接向 `master` 提交（master 为受保护分支，需 bypass 权限）。
- 发布后**保留最近一个 fix 分支**便于回溯，其余已合并分支可删除。

```bash
git checkout -b fix/my-change
# ... 开发 ...
git commit -m "fix: ..."
git push -u origin fix/my-change
# 验证通过后
git checkout master
git merge --no-ff fix/my-change -m "Merge fix/my-change: ..."
git push origin master
```

### 1.3 构建、测试与运行

```bash
# 编译 + 打包（fat jar，含测试）
./mvnw clean package

# 跳过测试快速构建（发布流水线使用此方式）
./mvnw clean package -DskipTests

# 仅运行测试
./mvnw test

# 运行应用（开发模式）
./mvnw spring-boot:run
# 或运行打包产物
java -jar target/WuZhu-1.0.jar
```

产物：`target/WuZhu-1.0.jar`（Spring Boot 3.2 **fat jar**，Main-Class 为 `org.springframework.boot.loader.launch.JarLauncher`）。

> ⚠️ **fat jar 仅供 `java -jar` 直接运行**。打包安装包时**不能**用它，必须用同目录的 **`target/WuZhu-1.0.jar.original`（薄 jar，Spring Boot repackage 前的原 jar）**——fat jar 的应用类在 `BOOT-INF/` 内，jpackage 的 classpath 启动方式无法加载（详见 §3.1 / §四）。
>
> ⚠️ 已知问题：仓库唯一的 `WuZhuApplicationTests`（Spring 上下文测试）在 master 上**本身就无法通过**——测试里 `wuzhu.javafx.enabled=false` 会禁用 `Workbench` Bean，但其它 Bean 仍依赖它；`Workbench` 是 final 类无法 mock，真实构造又需要 JavaFX 平台。发布流水线使用 `-DskipTests`，不受影响。若要修复，需引入 TestFX/Monocle 无头测试，作为独立任务处理。

### 1.4 代码约定

- 依赖注入使用 **Setter 注入**（`@Autowired setXxx(...)`）。
- 视图对象（VO）使用 JavaFX Property 支持 UI 绑定。
- `pom.xml` 中 lombok 必须放在 mapstruct 之前。
- 详细约定见 [`AGENTS.md`](../AGENTS.md)。

---

## 二、发布流程

### 2.1 版本号约定（tag 驱动）

| 项 | 约定 | 说明 |
|---|---|---|
| Git tag | `v1.0.4` | 唯一版本来源，格式 `v主.次.修订` |
| pom.xml `<version>` | 保持 `1.0`（开发版本） | **不改 pom**，发版号完全由 tag 决定 |
| jpackage `--app-version` | 由 tag 推导（`v1.0.4` → `1.0.4`） | 打包脚本自动剥离 `v` 前缀 |
| 安装包名 | `wuzhu_1.0.4_amd64.deb` / `WuZhu-1.0.4.msi` | jpackage 默认命名 |

> 采用 **tag 驱动**，避免“改了 pom 忘了提交”。**发布新版 = 确保 master 构建通过 + 打新 tag 并推送**。

### 2.2 一键发布（推荐：GitHub Actions 全自动）

流程完全自动化，发布者只需**打 tag 并推送**：

```bash
# 0. 前置检查：master 是最新、已合并所有修复、无未提交改动
git checkout master && git pull
git status           # 应为 clean
git log --oneline -3 # 确认最新修复已合入

# 1. 打 annotated tag 并推送 —— 触发 Actions
#    新版本号在现有版本基础上递增（如 v1.0.4 -> v1.0.5）
git tag -a v1.0.5 -m "Release v1.0.5"
git push origin v1.0.5
```

推送 tag 后，[`.github/workflows/release.yml`](../.github/workflows/release.yml) 自动执行：

```
v1.0.5 tag
   ├─ build-ubuntu  (ubuntu-24.04)  ──▶ ./packaging/package-ubuntu.sh ──▶ wuzhu_1.0.5_amd64.deb
   ├─ build-windows (windows-latest) ──▶ .\packaging\package-windows.ps1 ──▶ WuZhu-1.0.5.msi
   └─ release (ubuntu-latest, contents: write)
        ├─ 下载两个平台的构建产物
        └─ softprops/action-gh-release@v2 创建 Release 并附带 .deb + .msi
            （generate_release_notes: true 自动生成变更说明）
```

**验证步骤（Agent/人工都必须执行）：**

```bash
# 1. 等 Actions 跑完（两个 build job + 一个 release job 都绿）
gh run list --limit 3

# 2. 确认 Release 已发布且包含两个安装包
gh release view v1.0.5
#   期望看到:
#     asset:  WuZhu-1.0.5.msi
#     asset:  wuzhu_1.0.5_amd64.deb

# 3. 浏览器打开 https://github.com/mapleafly/WuZhu/releases 抽查
```

> - 只有 **Ubuntu 与 Windows 两个构建都成功**，`release` job 才会执行；任一失败则不会发布。
> - 若 `gh` 偶发网络/TLS 超时，重试即可（本环境实测偶发，重试能过）。

### 2.3 手动触发

流水线支持 `workflow_dispatch` 手动触发（Actions 页面 → Run workflow → 填写 `tag`，如 `v1.0.5`）：

- 手动触发时版本号取输入的 `tag`（不是分支名），与 2.2 行为一致。

### 2.4 修正后重发（已发版但有 bug）

标准的 tag 驱动重发流程——把 tag 移动到修复后的 commit 并强制推送：

```bash
# 1. 在分支上修复，合并到 master 并推送
git checkout -b fix/windows-release
# ... 修改、本地验证 ...
git commit -m "fix(win): ..."
git push -u origin fix/windows-release
git checkout master
git merge --no-ff fix/windows-release -m "Merge fix/windows-release: ..."
git push origin master

# 2. 移动 tag 到修复后的 master 并强制推送 —— 重新触发 Actions
git tag -f v1.0.5 -m "Release v1.0.5 (fix ...)"
git push -f origin v1.0.5
```

> 强制推送 tag 会触发同一 tag 的**新一次** Release 构建；Release 页面上旧的（已替换）资产会自动被新资产覆盖/更新。

### 2.5 本地打包（备选，无 CI 时）

不依赖 GitHub Actions 时，可本地直接出 Linux `.deb`（Windows `.msi` 需 Windows 环境，见 2.6）：

```bash
# Ubuntu 24.04 .deb（版本号参数化）
APP_VERSION=1.0.5 ./packaging/package-ubuntu.sh
# 或 ./packaging/package-ubuntu.sh 1.0.5
# 产物: target/dist/wuzhu_1.0.5_amd64.deb

# 创建 GitHub Release 并上传（需 gh CLI 已登录）
gh release create v1.0.5 \
  target/dist/wuzhu_1.0.5_amd64.deb \
  --title "WuZhu v1.0.5" \
  --generate-notes \
  --repo mapleafly/WuZhu
```

### 2.6 Windows 安装包为什么必须用 GitHub Actions

`jpackage` **不支持跨平台打包**：在 Linux 上只能出 `.deb`，Windows `.msi/.exe` 必须跑在 Windows 环境（jpackage 依赖目标平台的 WiX 等工具，`jlink` 同理）。因此：

- WSL2 本地能出：`.deb`、跨平台 fat jar。
- Windows `.msi/.exe`：**用 GitHub Actions 的 `windows-latest` runner**（见 2.2），或 Windows 真机/VM 上装 Liberica Full + WiX 手动打包。

---

## 三、打包脚本说明（发布正确配方，勿回退）

> 以下 5 点是 v1.0.1 → v1.0.4 逐步踩坑后的**最终正确配置**，任何一项改回旧值都会导致 Windows 启动报 `failed to launch JVM`。

### 3.0 五大关键点（速查）

| # | 关键点 | 错误做法（曾导致失败） | 正确做法 |
|---|---|---|---|
| 1 | 主 jar | 用 **fat jar**（`BOOT-INF/`，只能 `java -jar`） | 用 **薄 jar** `target/WuZhu-1.0.jar.original`，复制为 `target/dependency/WuZhu-1.0.jar` |
| 2 | main-class | `org.springframework.boot.loader.launch.JarLauncher`（fat jar 的启动器） | `org.lifxue.wuzhu.WuZhuApplication` |
| 3 | 内置 runtime | jpackage 自动生成的 runtime（缺 `bin\java.exe`） | 用 `jlink` 自建 `target/custom-jre`，`--runtime-image` 指定 |
| 4 | JavaFX 模块 | 不传 `--add-modules`（classpath 应用看不到内置 runtime 的 JavaFX） | `--java-options "--add-modules" "javafx.base,javafx.graphics,javafx.controls,javafx.fxml,javafx.web,javafx.media,javafx.swing"` |
| 5 | 依赖 scope | `-DincludeScope=compile`（漏掉 runtime scope 的 h2 驱动） | `-DincludeScope=runtime` + 排除 devtools/configuration-processor |

### 3.1 package-ubuntu.sh

```bash
./packaging/package-ubuntu.sh [版本号]
```

- 版本号优先级：**命令行参数 > 环境变量 `APP_VERSION` > 最近 git tag > 默认 `1.0.0`**，并自动剥离 `v` 前缀。
- 环境要求：Liberica JDK 21 Full（含 JavaFX），`jpackage`、`fakeroot` 可用。
- 流程：`mvnw clean package -DskipTests` → `dependency:copy-dependencies -DincludeScope=runtime ...` → 复制薄 jar → `jlink` 生成精简 JRE（模块路径用 `$JAVA_HOME/jmods`）→ `jpackage --type deb`。
- **`--main-class` 为 `org.lifxue.wuzhu.WuZhuApplication`**（应用主类，配合薄 jar 由 classpath 直接加载）。
- `--main-jar` 为 `WuZhu-1.0.jar`（即复制后的薄 jar）。
- `--runtime-image target/custom-jre` + `--add-modules` java-options（见 3.0）。

### 3.2 package-windows.ps1

```powershell
.\packaging\package-windows.ps1 -AppVersion 1.0.5
# 或 $env:APP_VERSION="1.0.5"; .\packaging\package-windows.ps1
```

- 版本号逻辑同 Linux 脚本（参数 > 环境变量 > git tag > 默认）。
- 需要管理员权限 + WiX Toolset 3.x（Actions 中 `choco install wixtoolset -y`）。
- ⚠️ **PowerShell 调用 `mvnw.cmd` 时，`-D` 参数必须加引号**，否则 `-DexcludeGroupIds=org.openjfx` 会被 `cmd.exe` 拆成 `.openjfx`，Maven 报 `Unknown lifecycle phase .openjfx`：
  ```powershell
  & .\mvnw.cmd dependency:copy-dependencies "-DincludeScope=runtime" "-DexcludeGroupIds=org.openjfx" "-DexcludeArtifactIds=spring-boot-devtools,spring-boot-configuration-processor" -q
  ```
- jpackage 参数：`--main-class org.lifxue.wuzhu.WuZhuApplication`、`--runtime-image target\custom-jre`、`--add-modules` java-options、`--win-per-user-install`。

### 3.3 pom.xml 打包 profile

`pom.xml` 内置两个可选 profile（`mainJar` + `mainClass`，jpackage-maven-plugin 1.7.4）：

```bash
# Linux 出 .deb
./mvnw clean package -Ppackage-deb
# Windows 出 .msi（需在 Windows 上执行，且装了 WiX）
.\mvnw.cmd clean package -Ppackage-msi
```

> 发布流水线用的是 3.1/3.2 的脚本（内部也会调 `mvnw package`），因此这些 profile **显式激活**（而非按 OS 自动激活），避免打包重复执行。两个 profile 与脚本一样使用 `includeScope=runtime` 复制依赖、薄 jar、`--runtime-image`。

---

## 四、常见问题与排错

### 4.1 Windows 启动报 `failed to launch JVM`（重点）

按历史排查顺序，共出现过 **3 个根因**，全部已修复并固化在脚本中。若再遇到，按此顺序检查：

| # | 根因 | 现象/检查 | 解决 |
|---|---|---|---|
| 1 | 用了 fat jar | 安装目录 `app\WuZhu-1.0.jar` 是 fat jar（>10MB，有 `BOOT-INF/`），启动即 `failed to launch JVM` | 改用薄 jar `WuZhu-1.0.jar.original`（~5MB，应用类在根部），main-class 用 `WuZhuApplication`（见 3.0 #1 #2） |
| 2 | 内置 runtime 缺 JVM 启动器 | `runtime\bin\java.exe` 不存在（jpackage 自动生成 runtime 的坑） | 用 `jlink` 自建 runtime（含 `bin\java.exe`），`--runtime-image` 指定（见 3.0 #3） |
| 3 | 缺 h2 等 runtime 依赖 | JVM 已启动、Spring banner 已打印，但报 `Cannot load driver class: org.h2.Driver` | `copy-dependencies` 用 `-DincludeScope=runtime`（见 3.0 #5） |

**诊断技巧（关键）：** 直接命令行启动内置 JVM 绕过 GUI 弹窗，可拿到真实报错：

```powershell
# 在安装目录下，用内置 JVM + cfg 里的 classpath 启动，把错误打到控制台
# Windows: 先确认 runtime\bin\java.exe 存在；再直接用 javaw 或 java 跑应用
& "C:\Users\...\WuZhu\runtime\bin\java.exe" `
  --module-path ... --add-modules ... -cp "$(Get-Content WuZhu.cfg | Select-String 'app.classpath' | ...)" `
  org.lifxue.wuzhu.WuZhuApplication
```

（实际排障中，用户把直接启动的完整日志贴回来，即可定位到 Spring/H2 层错误，而不是停留在 "failed to launch JVM" 弹窗。）

### 4.2 其它常见问题

| 现象 | 原因 | 解决 |
|---|---|---|
| Release 只有代码、没有安装包 | Windows/Ubuntu 构建失败，`release` job 被跳过 | 查看 Actions 运行日志，修复后按 2.4 重发 |
| Actions 报 `Unknown lifecycle phase .openjfx` | PowerShell 调 `mvnw.cmd` 时 `-DexcludeGroupIds=org.openjfx` 被 `cmd.exe` 拆分 | 给 `-D` 参数加引号（见 3.2） |
| 安装后启动报 `ClassNotFoundException: org.springframework.boot.loader.JarLauncher` | 用了 fat jar 时代的旧 main-class 路径 | main-class 用 `org.lifxue.wuzhu.WuZhuApplication`（见 3.0 #2） |
| 脚本报“不支持 `--print-module-path`” | JDK 21 没有该选项 | jlink 直接用 `$JAVA_HOME/jmods`（见 3.1） |
| `java`/`mvn` 找不到 | 未激活 mise | 执行 `eval "$(mise activate bash)"`，确认 `~/.bashrc` 已配置 |
| 本地 `mvn test` 失败（`No qualifying bean ... Workbench`） | 既有测试问题（见 1.3 注意） | 发布用 `-DskipTests`；修复需引入无头测试框架 |
| Windows 安装包无法在本地打包 | jpackage 不支持跨平台 | 用 GitHub Actions `windows-latest` runner（见 2.6） |

---

## 五、相关文档

| 文档 | 内容 |
|---|---|
| [AGENTS.md](../AGENTS.md) | 项目知识库、代码约定 |
| [packaging/DEVELOPMENT.md](../packaging/DEVELOPMENT.md) | 开发运行指南（详细命令） |
| [packaging/PACKAGING_UBUNTU.md](../packaging/PACKAGING_UBUNTU.md) | Ubuntu `.deb` 打包手册 |
| [packaging/PACKAGING_WINDOWS.md](../packaging/PACKAGING_WINDOWS.md) | Windows `.msi/.exe` 打包手册 |
| [packaging/README.md](../packaging/README.md) | 打包文档索引 + CI/CD 参考 |
| [docs/plans/RELEASE_AUTOMATION.md](./plans/RELEASE_AUTOMATION.md) | 发布自动化方案（A/B 方案选型） |
| [docs/plans/DEV_ENV_MISE.md](./plans/DEV_ENV_MISE.md) | mise 环境方案 |
| [docs/plans/WINDOWS_PACKAGING_WSL2.md](./plans/WINDOWS_PACKAGING_WSL2.md) | Windows 打包可行性方案 |

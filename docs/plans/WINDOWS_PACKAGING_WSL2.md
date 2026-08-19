# 在 WSL2 + Ubuntu 24.04 下打包 Windows 程序的可行性方案

> 问题：是否可以在 WSL2 + Ubuntu 24.04 下直接把本项目打包成 Windows 程序并生成安装包（.exe/.msi）？
> 生成时间：2025-08-19

---

## 一、核心结论（先说答案）

| 结论 | 说明 |
|------|------|
| **不能**直接用 `jpackage` 在 Linux/WSL2 下产出 Windows 安装包 | jpackage **不支持跨平台打包**：在哪个操作系统上运行 jpackage，就只能产出哪个操作系统的安装包（Linux 只出 .deb/.rpm，Windows 只出 .msi/.exe，macOS 只出 .dmg/.pkg） |
| **可以**在 WSL2 下产出"供 Windows 使用的 fat jar" | Java 编译产物（jar）是跨平台的，WSL2 里 `mvn package` 打出的 `WuZhu-1.0.jar` 可直接放到 Windows 上运行 |
| **推荐**用 GitHub Actions 的 Windows runner 来出 .msi/.exe | 在云端 Windows 机器上运行 jpackage，WSL2 只负责代码与触发（详见 §3） |
| **备选**：Windows VM / 真机 / Wine | 见 §4 讨论，均有明显短板 |

---

## 二、为什么 jpackage 不能跨平台？

这是 Oracle 官方设计约束，与 WSL2 无关：

- `jpackage` 依赖**目标平台的打包工具**：
  - Windows 的 `.msi` 依赖 **WiX Toolset**（`candle`/`light`）；
  - Linux 的 `.deb` 依赖 `dpkg-deb`、`.rpm` 依赖 `rpmbuild`；
  - macOS 的 `.dmg/.pkg` 依赖 Xcode 相关工具。
- jpackage 运行时还会探测宿主系统注册表/系统目录，无法在 Linux 上"假装"自己是 Windows。
- 结论：**WSL2（Linux）环境下运行的 jpackage 只能产出 Linux 安装包**；要出 Windows 安装包，必须在 Windows 环境中执行 jpackage。

### 附带说明：为什么 jlink 也不能跨平台

- 本项目打包流程里会先用 `jlink` 生成"精简 JRE"（`--runtime-image`）。
- `jlink` 同样**只能在对应平台的 JDK 上生成本平台的 runtime**（Windows JDK 出 Windows runtime）。
- 因此整个"jlink + jpackage"链路都要求"在 Windows 上跑 Windows JDK"。

---

## 三、推荐方案：WSL2 开发 + GitHub Actions 出 Windows 安装包 ⭐

### 3.1 整体架构

```
WSL2 (Ubuntu 24.04)
  ├── mise 管理 JDK(Liberica Full)+Maven
  ├── 日常开发、编译、跑测试
  ├── 本地可出 .deb（Linux 安装包）✓
  └── 推送 v* tag
        │
        ▼
GitHub Actions
  ├── ubuntu-24.04 runner ──▶ .deb
  └── windows-latest runner ──▶ .msi/.exe  （Windows 真环境）
        │
        ▼
GitHub Release（自动发布，见 RELEASE_AUTOMATION.md）
```

### 3.2 Windows runner 的关键配置

```yaml
jobs:
  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: liberica
          java-version: '21'
          java-package: jdk+fx    # Liberica Full（含 JavaFX 模块）
          cache: maven
      - name: Install WiX Toolset
        run: choco install wixtoolset -y
      - name: Build MSI
        run: .\packaging\package-windows.ps1
      - uses: actions/upload-artifact@v4
        with:
          name: windows-installer
          path: target/dist/*.msi
```

- `windows-latest` runner 就是一台真正的 Windows Server 2022/2025 机器，`jpackage --type msi` 可正常工作。
- `package-windows.ps1` 已有完整的 jpackage MSI 流程（需先修正 `--main-class`，见 `DOC_ACCURACY_AUDIT.md`）。

### 3.3 这条路的优点

- 不占本地资源；GitHub 免费额度足够（每月 2000 分钟 Linux + 一定 Windows 额度）。
- 每次打 tag 自动出两个平台安装包并发布（与 `RELEASE_AUTOMATION.md` 无缝衔接）。
- Windows 环境干净、可复现，避免本地 Windows 环境"脏"导致的问题。

---

## 四、备选方案（各有短板，供评估）

### 4.1 本机 Windows + WSL2 互通（如果主机是 Windows）

如果你的 WSL2 运行在 Windows 主机上（本项目正是），可以有两条"近水楼台"的路：

| 方式 | 做法 | 评价 |
|------|------|------|
| **WSL2 生成 jar，Windows 侧跑 jpackage** | WSL2 里 `mvn package` 产出 fat jar（跨平台），拷贝到 Windows 目录（如 `/mnt/c/...`），在 Windows 上装 Liberica Full JDK + WiX，手动/脚本跑 jpackage 出 .msi | ✅ 可行、简单；缺点是要在 Windows 侧装一套 JDK+WiX |
| **Windows 主机 + Windows 版 mise** | 在 Windows 主机上也装 mise，用同一套 `.mise.toml` 管理 JDK | ✅ 环境与 WSL2 一致；缺点是 Windows 与 WSL2 是两套安装 |

> 说明：WSL2 是 Linux 子系统，**无法直接运行 Windows 的 .exe/jpackage.exe**（WSL2 不执行 Windows PE 二进制）。所以"在 WSL2 里调用 Windows 的 jpackage"这条路走不通，必须切到 Windows 侧的终端/脚本执行。

### 4.2 Windows 虚拟机

- 在主机上用 Hyper-V / VirtualBox / VMware 装一个 Windows VM，在 VM 里跑 jpackage。
- 优点：完全本地、可复现。
- 缺点：占用资源；WSL2 里做不了嵌套虚拟化（除非主机开启嵌套虚拟化并直接跑 Linux，而不是 WSL2 内），配置成本高。

### 4.3 Wine（在 Linux 上模拟 Windows）

- 理论上有用 Wine 跑 `jpackage.exe` 的尝试，但 jpackage 强依赖 Windows 系统 API 与 WiX，Wine 下**成功率极低、不支持、难以维护**。
- 结论：**不推荐**。

### 4.4 Docker（Windows 容器）

- Windows 容器只能在 Windows 主机上运行（WSL2 的 Docker 是 Linux 容器），对本题无帮助。

---

## 五、其他可跨平台"提前做的事"

即使最终 .msi/.exe 要在 Windows 上出，以下几件事在 WSL2 里就能完成并固化：

1. **fat jar 产物**：`./mvnw clean package -DskipTests` → `target/WuZhu-1.0.jar`（可直接拷到 Windows 运行，`java -jar`）。
2. **Linux .deb**：`./packaging/package-ubuntu.sh` → `target/dist/wuzhu_*.deb`（WSL2 本地直接出）。
3. **绿色版 zip/tar.gz**：linux 侧可出 Linux 绿色版；Windows 绿色版需要 Windows runtime，不能跨平台出。
4. **打包流程的 pom 化**：把 jpackage 步骤做进 Maven profile，便于 CI 复用（见 §6）。

---

## 六、可选的进一步优化：把打包做进 Maven（配合 CI）

当前仓库里 `pom.xml` 的 `org.panteleyev:jpackage-maven-plugin:1.4.0` 配置的是 `<module>` 方式，**与 fat jar 项目不匹配且未绑定执行阶段**（详见 `DOC_ACCURACY_AUDIT.md` §三）。建议改为（或直接用脚本方式）：

```xml
<profiles>
  <profile>
    <id>package-msi</id>
    <activation><os><family>windows</family></os></activation>
    <build>
      <plugins>
        <plugin>
          <groupId>org.panteleyev</groupId>
          <artifactId>jpackage-maven-plugin</artifactId>
          <version>1.7.4</version>   <!-- 升级自 1.4.0 -->
          <configuration>
            <name>WuZhu</name>
            <appVersion>${project.version}</appVersion>
            <vendor>lifxue</vendor>
            <mainJar>${project.build.finalName}.jar</mainJar>
            <mainClass>org.springframework.boot.loader.launch.JarLauncher</mainClass>
            <type>MSI</type>
            <icon>src/main/resources/org/lifxue/wuzhu/images/wuzhu-96.ico</icon>
            <winMenu>true</winMenu>
            <winShortcut>true</winShortcut>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

- 用 `<mainJar>` + `<mainClass>`（fat jar 方式）而非 `<module>`。
- `<os><family>windows</family></os>` 激活条件，确保只有在 Windows 上构建时才执行 MSI 打包（完美配合 GitHub Actions 的 windows runner）。
- 同理可加 `<id>package-deb</id>` 的 Linux profile。

---

## 七、总结建议

| 需求 | 建议路径 |
|------|---------|
| WSL2 本地日常开发/编译/跑 JavaFX | ✅ mise 装 Liberica JDK 21 Full（见 `DEV_ENV_MISE.md`） |
| WSL2 本地出 Linux .deb | ✅ `./packaging/package-ubuntu.sh` |
| **出 Windows .msi/.exe** | ✅ **用 GitHub Actions windows runner**（推荐），或本机 Windows 侧装 JDK+WiX 跑 jpackage |
| 自动发布到 GitHub Releases | ✅ 见 `RELEASE_AUTOMATION.md`（方案 A） |

> 一句话：**WSL2 里能稳定产出 Linux 安装包；Windows 安装包必须在 Windows 环境（GitHub Actions Windows runner 是最省事的选择）里产出，两者通过 tag 触发自动衔接。**

# WuZhu 方案文档目录

本目录存放 **方案/计划类** 文档（不直接修改项目代码），供你确认后再执行。

## 文档索引

| 文档 | 内容 | 对应你的问题 |
|------|------|-------------|
| [DEV_ENV_MISE.md](./DEV_ENV_MISE.md) | WSL2+Ubuntu24.04 用 mise 配置 JDK/Maven 的方案；**证实 mise 可装 BellSoft Liberica JDK 21 Full** | 问题 2 |
| [RELEASE_AUTOMATION.md](./RELEASE_AUTOMATION.md) | 自动生成 Releases 并提交的流程（GitHub Actions 全自动 + 本地 gh CLI 半自动） | 问题 3 |
| [WINDOWS_PACKAGING_WSL2.md](./WINDOWS_PACKAGING_WSL2.md) | WSL2 下打包 Windows 程序的可行性结论与方案（推荐 GitHub Actions Windows runner） | 问题 4 |
| [DOC_ACCURACY_AUDIT.md](./DOC_ACCURACY_AUDIT.md) | README / AGENTS.md / packaging 文档与 pom.xml、代码的一致性审查 | 问题 1 |

## 核心结论速览

1. **文档审查**：README 技术栈版本过时（仍写 Spring Boot 2.7.10，实际 3.2.0）；AGENTS.md 有重复矛盾的技术栈表、Flyway 版本与 ddl-auto 描述有误；打包脚本/文档的 `--main-class` 用的是 Boot 2.x 旧路径（Boot 3.2 下会失败）。详见 [DOC_ACCURACY_AUDIT.md](./DOC_ACCURACY_AUDIT.md)。
2. **mise 装 JDK**：✅ `java@liberica-javafx-21`（如 `liberica-javafx-21.0.12+10`）即 BellSoft Liberica JDK 21 Full，含 JavaFX，mise 完全支持。详见 [DEV_ENV_MISE.md](./DEV_ENV_MISE.md)。
3. **Releases 自动化**：推荐 GitHub Actions（tag 触发 → Ubuntu/Windows runner 分别出 .deb/.msi → 自动建 Release）。详见 [RELEASE_AUTOMATION.md](./RELEASE_AUTOMATION.md)。
4. **WSL2 出 Windows 安装包**：jpackage 不支持跨平台，WSL2 只能出 .deb；Windows .msi/.exe 需在 Windows 环境（推荐 GitHub Actions Windows runner）产出。详见 [WINDOWS_PACKAGING_WSL2.md](./WINDOWS_PACKAGING_WSL2.md)。

## 待确认的执行清单（按优先级）

- [ ] 修复打包脚本/文档 `--main-class` → `org.springframework.boot.loader.launch.JarLauncher`（P0）
- [ ] 清理 AGENTS.md 重复技术栈表、修正 Flyway/ddl-auto（P0）
- [ ] 更新 README/README_CN 技术栈版本（P1）
- [ ] 新建 `.mise.toml` + 安装 Liberica Full + Maven（P1）
- [ ] 新建 `.github/workflows/release.yml`（P1）
- [ ] 修正 packaging/README.md 的 setup-java 参数（P1）
- [ ] pom.xml jpackage 插件改造或删除（P2）

> 以上均需你确认后另行执行，本目录文档不修改任何现有项目代码。

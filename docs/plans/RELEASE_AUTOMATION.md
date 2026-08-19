# WuZhu 自动生成 Releases 并提交的流程方案

> 目标：把"打版本 → 构建安装包 → 发布到 GitHub Releases"固化为可重复、可自动化的流程。
> 生成时间：2025-08-19

---

## 一、现状

| 项 | 现状 |
|----|------|
| GitHub 仓库 | `https://github.com/mapleafly/WuZhu` |
| Releases | **0 个**（从未发布过） |
| GitHub Actions | **无** `.github/workflows/` 目录 |
| Git Tag | 本地仅有 `V1.0`（未确认推送状态） |
| 打包脚本 | `packaging/package-ubuntu.sh`（.deb）、`packaging/package-windows.ps1`（.msi） |
| gh CLI | ✅ 已安装并由 mise 管理，已登录 `mapleafly`（repo + workflow 权限） |
| 版本号 | pom `version=1.0`；jpackage `--app-version 1.0.0`；文档示例 `v1.0.0` |

---

## 二、方案选型

### 方案 A：GitHub Actions 全自动发布（推荐 ⭐）

- **触发**：推送 `v*` tag（如 `v1.0.0`）时自动触发。
- **工作方式**：Action 在 Ubuntu/Windows runner 上分别构建 `.deb` 和 `.msi`，自动创建 GitHub Release 并附带安装包。
- **优点**：跨平台构建（Windows 打包必须有 Windows runner，见 `WINDOWS_PACKAGING_WSL2.md`）；发布记录完整；无需人工干预。
- **缺点**：需要仓库开启 Actions（免费额度足够）；首次配置较繁琐。

### 方案 B：本地脚本 + gh CLI 半自动发布

- **触发**：在 WSL2 本地执行一个脚本，完成"改版本 → 提交 → 打 tag → 推送 → gh 创建 Release"。
- **优点**：不依赖 CI；可控性强；可作为方案 A 的补充（例如只在有 Windows 机器时本地出 .msi）。
- **缺点**：Linux 侧只能出 `.deb`；Windows 安装包仍需 Windows 环境。

### 方案 C：Release Please（Google 维护，Conventional Commits）

- 自动根据 commit 信息（feat/fix/...）推算下一个版本号、生成 CHANGELOG、创建 PR 与 Release。
- **优点**：版本号管理最规范（需要改用 Conventional Commits 提交规范）。
- **缺点**：对现有 git 提交风格要求较高；引入额外工具。
- **建议**：现阶段选 **方案 A（主）+ 方案 B（备）**，等提交规范成熟后再考虑 Release Please。

---

## 三、方案 A 详细设计：GitHub Actions 全自动发布

### 3.1 仓库需要新增的文件

```
.github/workflows/release.yml   # 主发布工作流
.github/workflows/build.yml     # （可选）push/PR 时只做构建校验，不发布
```

### 3.2 发布工作流伪代码（release.yml）

```yaml
name: Release

on:
  push:
    tags: [ 'v*' ]
  workflow_dispatch:            # 支持手动触发（可选）
    inputs:
      tag:
        description: 'Release tag (e.g. v1.0.0)'
        required: true

jobs:
  build-ubuntu:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: liberica
          java-version: '21'
          java-package: jdk+fx     # Liberica Full（含 JavaFX）
          cache: maven
      - name: Build .deb
        run: ./packaging/package-ubuntu.sh
      - name: Upload .deb artifact
        uses: actions/upload-artifact@v4
        with:
          name: ubuntu-installer
          path: target/dist/*.deb

  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: liberica
          java-version: '21'
          java-package: jdk+fx
          cache: maven
      - name: Install WiX
        run: choco install wixtoolset -y
      - name: Build .msi
        run: .\packaging\package-windows.ps1
      - name: Upload .msi artifact
        uses: actions/upload-artifact@v4
        with:
          name: windows-installer
          path: target/dist/*.msi

  release:
    needs: [ build-ubuntu, build-windows ]
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
      - name: Download all artifacts
        uses: actions/download-artifact@v4
        with:
          path: artifacts/
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}          # v1.0.0
          name: WuZhu ${{ github.ref_name }}
          body: |                                    # 可接自动生成的 changelog
            ## 发布说明
            ...
          files: |
            artifacts/ubuntu-installer/*.deb
            artifacts/windows-installer/*.msi
          generate_release_notes: true              # GitHub 自动生成 release notes
```

### 3.3 关键点说明

| 关键点 | 说明 |
|--------|------|
| **`java-package: jdk+fx`** | 这是 setup-java 提供 JavaFX 的正确参数（项目 packaging/README.md 里写的 `javafx: true` 不存在，需修正） |
| **`softprops/action-gh-release@v2`** | 最流行的自动建 Release 的 Action；`generate_release_notes: true` 让 GitHub 根据 commits 自动生成 notes |
| **release job 权限** | 必须 `permissions: contents: write`，否则无法创建 Release |
| **tag 触发** | 约定 tag 形如 `v1.0.0`（与文档示例一致） |
| **产物** | `.deb`（Ubuntu）+ `.msi`（Windows），可选再加 tar.gz/zip 绿色版 |

### 3.4 发布流程（用户视角）

```bash
# 1.（可选）确认/调整版本号
# 2. 打 tag 并推送 → 触发 Actions
git tag v1.0.0
git push origin v1.0.0

# 3. Actions 自动构建并在 Releases 页生成 v1.0.0 及安装包
# 4. 如需修改说明，在 Releases 页编辑即可
```

---

## 四、方案 B 详细设计：本地脚本 + gh CLI（半自动）

> 适合：在 WSL2 本地快速发布 Linux 版本；或作为无 CI 时的兜底。

### 4.1 建议新建 `packaging/release-local.sh`

流程（伪代码）：

```bash
#!/usr/bin/env bash
set -euo pipefail

# 0. 校验 gh 登录
gh auth status

# 1. 从参数或 git 推导版本号（如 v1.0.0）
VERSION="${1:?usage: release-local.sh v1.0.0}"

# 2.（可选）更新 pom.xml 的 <version> 并提交 —— 见 §4.2
# 3. 构建 .deb（Linux）
./packaging/package-ubuntu.sh

# 4. 打 tag + 推送
git tag -a "$VERSION" -m "Release $VERSION"
git push origin "$VERSION"

# 5. 创建 Release 并上传 .deb
gh release create "$VERSION" \
  target/dist/wuzhu_*.deb \
  --title "WuZhu $VERSION" \
  --generate-notes \
  --repo mapleafly/WuZhu
```

### 4.2 "自动改版本并提交"的实现要点

由于要"自动生成 Releases 版本并提交"，通常有两种做法：

1. **不改 pom 版本，直接从 git tag 取版本号**（推荐）：
   - 保持 pom `<version>1.0</version>` 作为"开发版本"；
   - 发布版本号完全由 tag 决定，jpackage `--app-version` 也从 tag 推导（`v1.0.0` → `1.0.0`）；
   - 优点：无需每次发版改 pom，避免"改了 pom 忘了提交"。
2. **发版时自动改 pom 版本并提交**（mvn 方式）：
   ```bash
   mvn versions:set -DnewVersion=1.0.0 -DgenerateBackupPoms=false
   git add pom.xml
   git commit -m "chore(release): set version to 1.0.0"
   git tag v1.0.0
   ```
   - 需要 Maven（用 mise 装好后可用）。
   - 优点：pom 与 tag 严格一致。

> 建议默认用**做法 1**（tag 驱动），简单可靠；如你的团队习惯 pom 版本与发布一致，再改用做法 2。

---

## 五、版本号与命名约定（建议统一）

| 项 | 建议 | 现状/说明 |
|----|------|----------|
| Git tag | `v1.0.0` | 文档示例即用此格式 |
| pom `<version>` | 保持 `1.0` 或每次发版同步 | 建议 tag 驱动 |
| jpackage `--app-version` | 从 tag 推导（`v1.0.0`→`1.0.0`） | 现在脚本里写死 `1.0.0`，需参数化 |
| .deb 包名 | `wuzhu_1.0.0_amd64.deb`（jpackage 默认） | 与文档一致 |
| .msi 包名 | `WuZhu-1.0.0.msi`（jpackage 默认） | 与文档一致 |

---

## 六、落地步骤清单（待确认后执行）

| # | 动作 | 说明 |
|---|------|------|
| 1 | 新建 `.github/workflows/release.yml` | 方案 A 主流程 |
| 2 | （可选）新建 `.github/workflows/build.yml` | push/PR 构建校验 |
| 3 | 修正 packaging/README.md 的 setup-java 参数 | `java-package: jdk+fx` 替代 `javafx: true` |
| 4 | 参数化打包脚本的版本号（`--app-version` 从 tag 或参数推导） | 配合 tag 驱动 |
| 5 | 修复 `--main-class` 为 Boot 3.2 路径 | 见 `DOC_ACCURACY_AUDIT.md` P0 |
| 6 | （可选）新建 `packaging/release-local.sh` | 方案 B 本地发布脚本 |
| 7 | 首次发版：打 `v1.0.0` tag 试运行 | 验证全流程 |

> 注意：`.gitignore` 当前忽略 `target/`、`dist/`、`install/`，安装包不会误提交进仓库（只会进 Release 资产），符合预期。

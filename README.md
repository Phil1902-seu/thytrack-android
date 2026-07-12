# 甲友记 ThyTrack — Android Native

[![Android CI](https://github.com/Phil1902-seu/thytrack-android/actions/workflows/android.yml/badge.svg)](https://github.com/Phil1902-seu/thytrack-android/actions/workflows/android.yml)

甲状腺术后健康追踪应用 · **原生 Android（Kotlin + Jetpack Compose）** 重写版。

> ⚠️ 声明：本应用为个人健康管理工具，不能替代专业医疗诊断。

---

## 这是什么

本项目是 [Phil1902-seu/thytrack-flutter-lite](https://github.com/Phil1902-seu/thytrack-flutter-lite)（Flutter 跨平台版）的**原生 Android 重构**：
放弃 Flutter 引擎，改用 Kotlin + Jetpack Compose 实现，获得更小的安装包、更原生的交互与平台集成。
**数据 100% 兼容** Flutter 版导出的 CSV 与 WebDAV 备份，历史数据可无缝迁移。

| | Flutter 版 | 本仓库（Android Native） |
|---|---|---|
| 语言/UI | Dart / Flutter Widgets | Kotlin / Jetpack Compose (Material 3) |
| 架构 | Riverpod | MVVM + Hilt + StateFlow |
| 存储 | Hive | Room + DataStore |
| 图表 | fl_chart | Vico |
| 路由 | GoRouter | Navigation Compose |
| 最低版本 | Android 8.0 (API 26) | Android 8.0 (API 26) |

完整规格与实施计划见 [`SPEC.md`](./SPEC.md) 与 [`PLAN.md`](./PLAN.md)。

---

## 关于「Fork」

> ⚠️ 当前环境 GitHub OAuth 令牌失效，且 GitHub **不支持 fork 自己的仓库**（上游归属于 `Phil1902-seu`）。
> 因此本仓库以**新建仓库 `thytrack-android`** 的方式作为重写基座，本地已初始化 git 并设为上游引用。

待在 **CodeBuddy 设置 → 连接器** 重新授权 GitHub 后，将本仓库推送到：

```bash
# 在当前本地仓库根目录执行
git remote add origin https://github.com/Phil1902-seu/thytrack-android.git
git branch -M main
git push -u origin main
```

（若需保留上游对照，已配置 `upstream` 指向 Flutter 版：`git remote add upstream https://github.com/Phil1902-seu/thytrack-flutter-lite.git`）

---

## 技术栈

Kotlin 2.0 · Compose BOM 2024.12 · Hilt · Room(KSP) · Navigation Compose ·
Vico(图表) · Retrofit/OkHttp(WebDAV/OCR) · Apache Commons CSV · PdfBox-Android(PDF) ·
WorkManager/AlarmManager(通知) · DataStore · PhotoPicker。

---

## 项目结构

```
app/src/main/java/com/thytrack/android/
├── domain/model/        领域模型：LabRecord / MedicationChange / PatientInfo / RefRange / Drug
├── data/
│   ├── local/           ReferenceRanges（参考范围）、MetricDefinitions（29 指标/7 组）
│   ├── local/room/      Room 实体 / DAO / 数据库 / 类型转换 / 实体↔领域映射
│   └── repository/      RecordRepository / MedicationRepository / SettingsRepository
├── ui/                  记录列表（已实现数据流）/ 趋势 / 用药 / 设置（占位）
├── navigation/          底部导航 + NavHost 壳
├── di/                  Hilt 模块
├── util/                RefRangeManager / ValueValidator / FollowUpScheduler / XAxisStride
├── ThyTrackApplication.kt
└── MainActivity.kt
```

---

## 数据兼容

- **CSV**：表头与列顺序逐字对齐 Flutter 版，可双向导入导出。
- **WebDAV 备份**：JSON 结构一致；原生版额外补全 `custom_ref_ranges` 字段（Flutter 版缺失），读端容错。
- **参考范围**：29 项默认值与单位端口化，异常判定与 Flutter 版一致。

---

## 构建

需 **Android SDK（compileSdk 35）+ JDK 17**。

```bash
# 克隆后（或重新授权推送后）
./gradlew assembleDebug      # 调试 APK
./gradlew assembleRelease    # 发布（需配置签名）
```

首次在 Android Studio 打开时 Gradle 会解析依赖；版本号见 `gradle/libs.versions.toml`，可按需升级。

**CI 发布签名**：`android.yml` 含 `build`（debug）与 `release`（签名 release）两个作业。release 作业在 push 到 `main` 且仓库配置了 `KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` 四个 secret 时自动解码密钥库并执行 `assembleRelease`，产物上传为 `thytrack-release-apk`。本地无密钥时 `signingConfigs` 自动跳过，`assembleDebug` 不受影响。

---

## 实施进度（见 PLAN.md）

| Phase | 内容 | 状态 |
|-------|------|------|
| 0 | 项目基座与 CI | ✅ 脚手架就绪 |
| 1 | 数据层（Room/参考范围/指标） | ✅ 已实现 |
| 2 | 记录列表/录入/详情/草稿 | ✅ 已实现 |
| 3 | 趋势图（Vico） | ✅ 已实现 |
| 4 | 用药/设置/通知 | ✅ 已实现 |
| 5 | CSV/WebDAV/i18n | ✅ 已实现 |
| 6 | OCR/PDF/收尾 + 发布签名 | ✅ 全部完成（CI 双作业 green，release 已签名） |

---

Built with ❤️ for the thyroid community 🦋

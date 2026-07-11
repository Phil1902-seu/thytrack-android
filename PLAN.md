# ThyTrack Android Native — 实施计划（PLAN）

> 配套文档：[SPEC.md](./SPEC.md)
> 计划版本：v1.0 · 更新：2026-07-11
> 目标：以原生 Android（Kotlin + Jetpack Compose）重构 `thytrack-flutter-lite`，数据 100% 兼容。

---

## 0. 总体策略与里程碑

| Phase | 名称 | 主要产出 | 预计工期* |
|-------|------|----------|-----------|
| 0 | 项目基座与 CI | 可编译空壳 + Gradle/依赖/导航骨架 + CI 出 APK | 2–3 天 |
| 1 | 数据层 | Room 实体/DAO/DB/迁移 + 参考范围 + 指标定义 + 单测 | 4–5 天 |
| 2 | 核心 CRUD UI | 记录列表 / 录入编辑 / 详情 + 草稿 | 6–8 天 |
| 3 | 趋势图 | Vico 集成、参考带、异常着色、双轴、用药标记 | 5–6 天 |
| 4 | 用药 + 设置 + 通知 | 用药时间线、5 面板设置、WorkManager/AlarmManager | 5–6 天 |
| 5 | 数据互通 | CSV 导入导出、WebDAV 备份、i18n | 5–7 天 |
| 6 | OCR + PDF + 收尾 | OCR 校对、PDF 报告、打磨、发版 | 6–8 天 |

\* 工期为单人估算，含联调与基础自测；不含设计评审等待。

**依赖关系**：Phase 0 → 1 →（2 ∥ 3）→ 4 → 5 → 6。Phase 3（图表）可在 Phase 2 列表完成后并行启动。

**完成标准（DoD）**：所有 requirements 15 组 AC 通过；CSV/WebDAV 与 Flutter 版往返一致；单元测试覆盖率 ≥ 60%（核心域逻辑）；Release AAB 可安装运行。

---

## Phase 0 — 项目基座与 CI

**目标**：建立可编译、可运行的原生工程骨架，定义包结构与依赖基线。

任务：
1. 初始化 Gradle Kotlin DSL + Version Catalog（`libs.versions.toml`）；AGP 8.7、Kotlin 2.0、minSdk 26、compile/target 35、Java 17。
2. 引入基线依赖（见 SPEC §3 对照表）：Hilt、Room(KSP)、Navigation Compose、Vico、Retrofit+OkHttp、commons-csv、pdfbox-android、WorkManager、DataStore、PhotoPicker、Compose BOM、Material 3。
3. `ThyTrackApplication`（Hilt `@HiltAndroidApp`）+ `MainActivity`（`ComponentActivity` + `setContent`）。
4. Navigation Compose 底部导航壳（记录/趋势/用药/设置 4 个目的地 + FAB 新增）。
5. 主题：`Material3` 亮/暗（跟随系统）；`colors.xml` seed 色对齐 Flutter `#4A90D9`。
6. `AndroidManifest.xml`：网络、POST_NOTIFICATIONS（13+）、SCHEDULE_EXACT_ALARM、文件权限（低版本）；`ic_launcher` 占位。
7. CI（GitHub Actions `android.yml`，复刻上游）：`./gradlew assembleRelease` 出 APK 并上传 Artifact/Release。

产出：可 `assembleDebug` 的空壳；CI 绿。

---

## Phase 1 — 数据层

**目标**：锁定数据模型与兼容性，提供单一数据源。

任务：
1. 领域模型：`LabRecord`、`MedicationChange`、`PatientInfo`、`RefRange`、`Drug`（Kotlin data class / enum）。
2. Room 实体：`LabRecordEntity`（29 指标列 nullable + 元数据 + `customRefRanges` JSON）、`MedicationEntity`。
3. `TypeConverters`：`Date↔Long`、`Drug↔String`、`Map<String,RefRange>↔JSON`。
4. DAO：`LabRecordDao`（insert/upsert/delete/deleteAll/query by id/按日期倒序/按日期区间）、`MedicationDao`。
5. `AppDatabase`（v1）+ `AppDatabaseCallback`（启动执行 `migrateHospitalNames`（去 `#` 前缀）、`dedupeRecords` 逻辑，对齐 Flutter）。
6. `ReferenceRanges` 常量（逐值端口化）+ `MetricDefinitions`（7 组 29 指标：key/label/unit/默认范围/分组）。
7. `RefRangeManager`、`ValueValidator`、`FollowUpScheduler`、`XAxisStrideCalculator`（逻辑 1:1 移植）。
8. 仓储接口与实现：`RecordRepository`、`MedicationRepository`（ suspend + Flow 封装）。
9. 单测：上述工具类 + DAO 映射（Robolectric/Instrumented）。

产出：数据层可单测通过；`RecordRepository` 提供 `Flow<List<LabRecord>>`。

---

## Phase 2 — 核心 CRUD UI

**目标**：记录列表、录入/编辑、详情，实现日常记录闭环。

任务：
1. `RecordListViewModel` + `RecordListScreen`：倒序列表（日期/医院/TSH/FT4/Tg 关键值）、搜索栏、排序切换（偏好存 DataStore）、长按菜单、多选 `Selection` + 批量删、空态。
2. `RecordEditViewModel` + `RecordEditScreen`：7 组 `ExpansionTile` 表单；每个指标 decimal 输入 + 单位 + 参考范围提示；实时校验黄警；保存/新增；自定义参考范围编辑（仅本条）。
3. `DraftService`（DataStore）：录入中断自动保存（输入变化 30s 防抖），重开提示恢复。
4. `RecordDetailScreen`：指标卡 + 参考范围 + ↑/↓；「编辑」入口。
5. 删除确认 `AlertDialog`；批量删确认。
6. 单测：ViewModel 行为（Turbine）、草稿恢复、校验逻辑。

产出：可完整录入/查看/删除记录；草稿可用。

---

## Phase 3 — 趋势图

**目标**：可视化指标趋势，覆盖 requirements §6 全部 10 条 AC。

任务：
1. `TrendsViewModel` + `TrendsScreen`：指标选择器（7 组下拉/分段）、图表区。
2. Vico `LineChart` 封装 `MetricChart`：数据点（正常蓝/异常红）、连线跳过 nil。
3. 参考范围半透明绿带（`LineCartesianLayer` + `Area` 或自定义 decoration）。
4. 用药竖线标记（`MedicationChange` 日期对齐）。
5. X 轴自适应（`XAxisStrideCalculator`），斜向/旋转标签。
6. TSH 双轴：叠加优甲乐剂量（右轴）。
7. 点选 `Tooltip`（数值 + 日期）；水平缩放/平移（Vico `zoom`/手势）。
8. 降级方案：若 Vico 标注能力不足，改用 MPAndroidChart + `AndroidView`。

产出：交互式趋势图；与 Flutter 视觉一致。

---

## Phase 4 — 用药 + 设置 + 通知

**目标**：用药追踪、设置面板、本地复诊提醒。

任务：
1. `MedicationViewModel` + `MedicationTimelineScreen`：按日期升序时间线；新增/编辑/删除（药物/old→new 剂量/原因/关联 recordId）。
2. 设置 5 面板（`SettingsScreen` + 子项）：
   - 通用：主题、语言入口、数据迁移入口。
   - 复查提醒：间隔（3/6/自定义月）、提前天数（默认 7）、开关。
   - OCR：隐私同意状态。
   - 备份：WebDAV 入口（见 Phase 5）。
   - 关于：版本号（`PackageInfo`）、开源许可。
3. `PatientInfo` 编辑（DataStore），供 PDF 页眉。
4. 通知：`FollowUpScheduler` + `WorkManager`(弹性) + `AlarmManager`(精确，需 `SCHEDULE_EXACT_ALARM`)；逾期红色「已逾期」；开关关闭取消所有。
5. 权限：`POST_NOTIFICATIONS`（Android 13+）引导；精确闹钟引导。

产出：用药闭环 + 设置齐全 + 复诊提醒可达。

---

## Phase 5 — 数据互通（CSV / WebDAV / i18n）

**目标**：与 Flutter 版无缝迁移，双语支持。

任务：
1. `CsvRepository`：导出（列顺序/表头逐字对齐，见 SPEC §5.1）、导入（按表头匹配、容错跳过、日期三格式、重复处理「跳过/覆盖/保留」）、统计（成功/跳过/错误）。
2. `CsvScreen`：导入导出入口、结果统计展示、系统分享（`Intent.ACTION_SEND`）。
3. `WebDavRepository`（OkHttp）：`PROPFIND` 测试连接 / `PUT` 备份（含 `custom_ref_ranges` 修复）/ `PROPFIND` 列文件 / `GET` 恢复；凭据 `EncryptedSharedPreferences`+Keystore。
4. `BackupScreen`：配置、测试、备份列表、恢复确认。
5. i18n：`strings.xml`（`values` 中文默认 + `values-en` 英文），覆盖全部用户可见文本；跟随系统。

产出：CSV/WebDAV 与 Flutter 版往返一致（专项测试）；中英双语。

---

## Phase 6 — OCR + PDF + 收尾

**目标**：补齐识别与报告能力，达到发版质量。

任务：
1. `OcrRepository`（Retrofit/OkHttp）：`/health` 探测、`/ocr` 识别（图片字节 + 30s 超时 + 取消 + 重试）。
2. `OcrImportScreen` + `OcrReviewScreen`：左图右表校对；TG 歧义高亮 + 手动选择器（甲状腺球蛋白/甘油三酯）；越界黄警；存为 `source=ocr` 并追加备注；首次使用隐私同意弹窗。
3. `PdfExportService`（PdfBox-Android）：A4、页眉、指标表（异常标红+↑↓）、每条独立页、nil 显示 `-`、页脚页码；系统打印/分享。
4. 打磨：空态/错误态/加载态、无障碍（contentDescription）、深色模式走查、性能（大列表/图表）。
5. 发版：`versionCode`/`versionName`（建议 `2.0.0` 起）、签名配置、Release AAB + APK、更新 README 与 CHANGELOG、GitHub Release。

产出：功能完整、可发版的 `thytrack-android`。

---

## 专项：数据兼容与迁移

- **契约唯一来源**：以 Flutter 版 `csv_service.dart` 列定义、`webdav_service.dart` JSON 结构、`reference_ranges.dart` 范围为基准，写兼容测试锁定。
- **测试样本**：从 Flutter 版导出 1 份 CSV + 1 份 WebDAV 备份，纳入 `androidTest/assets`，断言原生端导入后字段零丢失。
- **迁移路径**：用户侧「Flutter 导出 → 原生导入」，无需直接转换 Hive 二进制。

---

## 风险登记（持续更新）

| 风险 | 影响 | 应对 |
|------|------|------|
| Android SDK/编译环境缺失（当前沙箱无 SDK） | 无法本地编译验证 | 在 Android Studio 环境首次 sync 时校验 Gradle/版本；CI 兜底 |
| Vico 双轴/标注能力 | 趋势图复杂度 | Phase 3 预留 MPAndroidChart 降级 |
| 精确闹钟受限 | 提醒准时性 | WorkManager 保底 + 精确闹钟授权引导 |
| OCR 服务依赖 | 识别可用性 | 健康探测 + 超时重试 + 离线提示 |

---

## 下一步（待评审）

1. 确认本计划与 SPEC，冻结范围。
2. 在具备 Android SDK 的环境执行 Phase 0（或先由本仓库提供脚手架，本地 sync 校验）。
3. 按 Phase 1→6 顺序推进，每阶段结束回归数据兼容测试。

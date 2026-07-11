# ThyTrack Android Native — 产品与技术规格（SPEC）

> 项目代号：`thytrack-android`
> 上游参考仓库：[Phil1902-seu/thytrack-flutter-lite](https://github.com/Phil1902-seu/thytrack-flutter-lite)（Flutter 跨平台版）
> 文档版本：v1.0 · 更新：2026-07-11
> 状态：规格冻结（待实施计划评审通过后进入编码）

---

## 1. 背景与目标

**甲友记 ThyTrack** 是一款面向甲状腺术后患者的健康追踪应用，用于记录甲状腺功能、血糖血脂、肝肾功能、电解质骨代谢等关键检验指标，并通过趋势图、用药时间线、CSV/WebDAV 备份、PDF 报告、本地复诊提醒等手段辅助长期随访管理。

现有实现基于 Flutter（Dart），可同时产出 iOS/Android 包。本次以**原生 Android（Kotlin + Jetpack Compose）** 路线重构，目标如下：

| 目标 | 说明 |
|------|------|
| 平台专注 | 仅面向 Android，放弃 iOS；换取更小的安装包、更低的内存占用与更顺滑的滚动/图表交互 |
| 平台原生体验 | 使用 Material 3、系统深色模式、原生分享面板、PhotoPicker、精确闹钟，贴合 Android 设计规范 |
| 数据兼容 | **100% 兼容** Flutter 版导出的 CSV 与 WebDAV 备份 JSON，用户可无缝迁移历史数据 |
| 离线优先 | 全部数据本地存储（Room + DataStore），无账号体系、无云端强制上传，隐私数据不出设备 |
| 可维护 | 采用 Clean Architecture + MVVM，依赖注入（Hilt），单一数据源，可单测 |

**不做的事**：不重写 OCR 云端服务（复用现有 `ocr_proxy.py` HTTP 契约）；不引入跨平台方案；首版不实现 iOS。

---

## 2. 范围与平台定位

| 项 | 取值 |
|----|------|
| 语言 | Kotlin 2.0+（JVM 17） |
| UI | Jetpack Compose（Material 3） |
| minSdk | **26**（Android 8.0，与 Flutter 版一致） |
| targetSdk / compileSdk | **35**（Android 15） |
| 架构 | Clean Architecture + MVVM + 单向数据流（StateFlow） |
| 构建 | Gradle Kotlin DSL + Version Catalog + KSP |
| 依赖注入 | Hilt |
| 发行 | AAB（Play）/ APK（侧载，GitHub Release） |

> 选择 Compose 而非 View/XML：与 Flutter 的声明式范式最贴近，迁移心智成本低，且官方已全面转向 Compose。

---

## 3. 总体架构

```
┌───────────────────────────────────────────────────────────┐
│ UI 层 (ui/)  Compose 可组合项 + ViewModel (StateFlow)        │
│   记录列表 / 录入编辑 / 详情 / 趋势图 / 用药 / 设置 / OCR / 备份│
├───────────────────────────────────────────────────────────┤
│ 领域层 (domain/)  模型 + 仓储接口 + 用例(UseCase)            │
│   LabRecord, MedicationChange, PatientInfo, RefRange, Drug   │
│   RecordRepository, BackupRepository, OcrRepository …        │
├───────────────────────────────────────────────────────────┤
│ 数据层 (data/)  Room(DB) + DataStore + Retrofit/OkHttp       │
│   local/room (实体/DAO/TypeConverter)  remote (WebDAV/OCR)   │
├───────────────────────────────────────────────────────────┤
│ 基础设施  di(Hilt) / util(校验,参考范围,X轴,复诊计算) / i18n │
└───────────────────────────────────────────────────────────┘
```

| 关注点 | Flutter 版 | Android Native 版 |
|--------|-----------|-------------------|
| 应用入口 | `main.dart` | `ThyTrackApplication` + `MainActivity` + `NavGraph` |
| 状态管理 | Riverpod `StateProvider`/`Provider` | Hilt `ViewModel` + `StateFlow`/`SharedFlow` |
| 本地存储 | Hive（`records`/`medications`/`settings` Box） | Room（`records`/`medications` 表）+ DataStore（`settings`/`patient`/`draft`） |
| 路由 | GoRouter `ShellRoute` | Navigation Compose（底部导航 + 嵌套图） |
| 图表 | `fl_chart` | **Vico**（`io.github.raamcosta.compose-destinations` 仅导航；图表用 Vico；MPAndroidChart 为降级备选） |
| HTTP | `dio` | **Retrofit + OkHttp**（WebDAV 用 OkHttp 原生请求：`PROPFIND/PUT/GET`） |
| PDF | `pdf` + `printing` | **PdfBox-Android**（`com.tom-roush:pdfbox-android`，Apache-2.0）+ 打印框架 |
| CSV | `csv` | **Apache Commons CSV**（`org.apache.commons:commons-csv`） |
| 通知 | `flutter_local_notifications` | **WorkManager**（弹性）+ **AlarmManager**（精确，`SCHEDULE_EXACT_ALARM`）+ `NotificationManager` |
| 图片选择 | `image_picker` | **Android PhotoPicker**（`PickVisualMedia`，API 33+；低版本回退 `ACTION_GET_CONTENT`） |
| 分享 | `share_plus` | 系统 Sharesheet（`Intent.ACTION_SEND`/`ACTION_SEND_MULTIPLE`） |
| 国际化 | `flutter_localizations` (arb) | Android 资源 `strings.xml`（`values` 默认中文 / `values-en` 英文） |
| 代码生成 | `build_runner`（Hive Adapter） | **KSP**（Room、Hilt） |

---

## 4. 数据模型（Room）

### 4.1 LabRecord（化验记录）

采用**宽表**策略（与 Hive 字段一一对应，便于 CSV 双向无损映射），单表 `records`：

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | TEXT (PK) | UUID，唯一 |
| `date` | INTEGER | 检查日期（epoch millis） |
| `hospital` | TEXT | 医院 |
| `notes` | TEXT | 备注 |
| `created_at` / `updated_at` | INTEGER | 时间戳（epoch millis） |
| `source` | TEXT | `manual` / `csv` / `ocr` |
| `schema_version` | INTEGER | 默认 1 |
| **甲状腺功能** | | |
| `tsh` `ft3` `ft4` `tt3` `tt4` `tg` `tgab` `tpoab` | REAL (nullable) | 8 项 |
| **血糖** | | |
| `fpg` `two_hpg` `hba1c` | REAL (nullable) | 3 项 |
| **血脂** | | |
| `tc` `tg_lipid` `hdl` `ldl` | REAL (nullable) | 4 项 |
| **肝功能** | | |
| `alt` `ast` `tbil` | REAL (nullable) | 3 项 |
| **肾功能** | | |
| `cr` `bun` `ua` `egfr` | REAL (nullable) | 4 项 |
| **电解质与骨代谢** | | |
| `calcium` `phosphorus` `pth` `vitamin_d` | REAL (nullable) | 4 项 |
| **关联用药** | | |
| `levothyroxine_dose` `calcium_dose` `calcitriol_dose` | REAL (nullable) | 3 项 |
| `custom_ref_ranges` | TEXT (nullable) | JSON：`{key:{low,high}}`，仅作用本条记录 |

> 29 个指标列全部 **nullable**：`null` = 本次未检测（不可与 `0` 混淆，沿用 Flutter 不变式）。

### 4.2 MedicationChange（用药变更）

表 `medications`：`id`(PK) / `date` / `drug`(TEXT 枚举名) / `old_dose`(REAL) / `new_dose`(REAL) / `reason`(TEXT) / `created_at` / `updated_at` / `record_id`(TEXT nullable)。

`Drug` 枚举：`levothyroxine`(优甲乐) / `calcium`(钙片) / `calcitriol`(骨化三醇)。

### 4.3 PatientInfo（患者信息）

存于 DataStore `Preferences`（键 `patient_name` / `patient_age` / `patient_surgery_date` / `patient_pathology`），供 PDF 页眉使用。字段：`name` / `age` / `surgeryDate` / `pathology`。

### 4.4 参考范围（端口化，必须逐值一致）

`ReferenceRanges` 对象常量（单位与 Flutter 完全一致）：

| 指标 | 范围 | 指标 | 范围 |
|------|------|------|------|
| tsh | 0.27–4.2 | fpg | 3.9–6.1 |
| ft3 | 3.1–6.8 | two_hpg | 0–7.8 |
| ft4 | 12–22 | hba1c | 4.0–6.0 |
| tt3 | 0.9–2.5 | tc | 0–5.18 |
| tt4 | 58–161 | tg_lipid | 0–1.70 |
| tg | 0–0.2 | hdl | 1.04–99 |
| tgab | 0–115 | ldl | 0–3.37 |
| tpoab | 0–34 | alt | 7–40 |
| | | ast | 13–35 |
| | | tbil | 5.1–28.0 |
| | | cr | 44–133 |
| | | bun | 2.9–8.2 |
| | | ua | 149–416 |
| | | egfr | 90–999 |
| | | calcium | 2.15–2.55 |
| | | phosphorus | 0.81–1.45 |
| | | pth | 15–65 |
| | | vitamin_d | 30–100 |

> 数值合理性校验阈值（`ValueValidator.sanityCheck`，如 tsh 0.01–100、ft4 1–100 等）一并端口化，用于录入/校对时的黄色警告。

---

## 5. 数据兼容性与迁移（关键）

### 5.1 CSV 格式（完全保留）

导出列顺序与表头**逐字对齐** Flutter 版，确保双向可导入：

```
id,date,hospital,notes,source,created_at,updated_at,schema_version,
tsh,ft3,ft4,tt3,tt4,tg,tgab,tpoab,
fpg,two_hpg,hba1c,
tc,tg_lipid,hdl,ldl,
alt,ast,tbil,
cr,bun,ua,egfr,
calcium,phosphorus,pth,vitamin_d,
levothyroxine_dose,calcium_dose,calcitriol_dose,
custom_ref_ranges
```

- 导入按**表头名匹配列**（不依赖顺序），未知列忽略并记录日志；
- 数值格式错误行跳过并统计；日期支持 `ISO 8601` / `yyyy-MM-dd` / `yyyy/MM/dd`；
- 重复判定（同日 + 同医院）提供「跳过 / 覆盖 / 保留两份」；
- nil 字段导出为空字符串，导入空串还原为 `null`。

### 5.2 WebDAV 备份 JSON（向后兼容 + 修复）

保留原结构 `{version, exported_at, records[], medications[]}`。
**修复项**：原生版在 `records` 每项中**补充 `custom_ref_ranges` 字段**（Flutter 版缺失），读端对缺失该字段容错（回退空）。备份文件命名 `thytrack_backup_YYYYMMDD_HHMMSS.json` 不变。

### 5.3 从 Flutter 版迁移

提供「设置 → 数据迁移」：用户从 Flutter 版导出 CSV（或 WebDAV 恢复）后，在原生版导入即可。不做直接数据库转换（Hive 二进制不可读），统一走 CSV/WebDAV 标准通道。

---

## 6. 功能模块规格（与 Flutter 版逐项对齐）

| # | 模块 | 原生实现要点 | 验收（对齐 requirements） |
|---|------|-------------|--------------------------|
| 1 | 记录列表 | Compose `LazyColumn`，按日期倒序；搜索（日期/医院/备注实时过滤）；长按菜单（编辑/删除/详情）；多选批量删除；排序偏好存 DataStore | 倒序、搜索、批量删、确认弹窗 |
| 2 | 录入/编辑 | 7 个 `Group` 分组 `Card`/`ExpansionTile`；每个指标 `OutlinedTextField`(decimal) + 单位 + 参考范围提示；实时校验（超 10× 范围黄警）；草稿自动保存（DataStore，30s）；自定义参考范围仅作用本条 | 分组表单、草稿恢复、自定义范围隔离 |
| 3 | 记录详情 | 指标卡 + 参考范围带 + ↑/↓ 标记；可跳转编辑 | 详情展示、异常标记 |
| 4 | 趋势图 | Vico `LineChart`：参考范围半透明绿带；异常点红/正常点蓝；用药竖线标记；X 轴自适应（<6月按月 / 6月–2年按季 / >2年按年）；TSH 叠加优甲乐剂量双轴；点选显示数值/日期；nil 不连线不计数 | 10 条 AC 全部覆盖 |
| 5 | 用药管理 | 时间线（按日期升序）；CRUD；剂量变更 old→new + 原因 + 可关联 recordId | 新增/时间线/编辑 |
| 6 | CSV | 见 §5.1 | 7 条 AC |
| 7 | PDF | PdfBox-Android 生成 A4：页眉(患者/手术日期/生成日期)、标题、指标表(异常标红+↑↓)、每条独立页、nil 显示 `-`、页脚页码；经系统打印/分享 | 5 条 AC |
| 8 | OCR | 调用现有 `ocr_proxy.py`（`/health`、`/ocr`）；图片 JPEG/PNG/HEIC，>10MB 提示压缩；进度+取消+30s 超时；校对页(左图右表)；TG 歧义高亮+手动选择器；值越界黄警；存为 `source=ocr` 并备注 | 7 条 AC |
| 9 | WebDAV | OkHttp 实现 `PROPFIND/PUT/GET`；凭据 DataStore 加密存储；测试连接；备份列表+恢复 | 5 条 AC |
| 10 | 通知 | `WorkManager`(弹性) + `AlarmManager`(精确，`SCHEDULE_EXACT_ALARM`)；间隔 3/6/自定义月；提前 N 天(默认7)提醒；逾期红色「已逾期」；开关关闭则不提醒 | 4 条 AC |
| 11 | 设置 | 5 面板：通用/复查提醒/OCR/备份/关于；患者信息编辑；复查间隔；版本号+许可 | 5 条 AC |
| 12 | 移动适配 | 底部 TabBar；图表水平缩放/平移；表单可滚动；触摸目标 ≥ 48dp（Material 默认）；相机/相册 | 5 条 AC |
| 13 | 国际化 | `values`(zh-CN 默认) + `values-en`；跟随系统；默认中文 | 3 条 AC |

---

## 7. 图表规格（趋势）

- **参考范围带**：在 `low`/`high` 之间绘制半透明绿色 `Area`，超出区域无填充。
- **数据点着色**：`value < low || value > high` → 红色；否则蓝色。
- **用药标记**：在数据日期匹配 `MedicationChange.date` 处绘制竖向参考线，标注剂量变更。
- **X 轴自适应**：`XAxisStrideCalculator` 复刻 Flutter 逻辑（见 §4 跨度阈值）。
- **TSH 特例**：选中 TSH 时叠加优甲乐剂量曲线（右轴 `levothyroxine_dose`）。
- **交互**：点击/长按数据点弹出 `Tooltip`（数值 + 日期）。

---

## 8. 隐私与安全

- 全部数据位于应用沙盒（`/data/data/com.thytrack.android/`），仅本应用可访问。
- WebDAV 凭据：DataStore 中以 **Android Keystore + EncryptedSharedPreferences** 加密存储；传输强制 HTTPS + Basic Auth。
- 不收集任何遥测；OCR 图片经用户同意后上传至自有服务。
- 数据库迁移使用 Room `Migration`，保留旧数据。

---

## 9. 测试策略

| 层级 | 框架 | 覆盖 |
|------|------|------|
| 单元测试 | JUnit5 + AssertJ + Turbine | `RefRangeManager`、数值校验、`FollowUpScheduler`、`XAxisStrideCalculator`、CSV 解析（含 TG 歧义）、Repository 映射 |
| 数据兼容测试 | JUnit | Flutter CSV 样本导入/导出往返一致；WebDAV JSON 读旧写新 |
| UI 测试 | Compose UI Test | 录入表单校验/草稿、列表搜索/批量删、趋势图渲染 |
| 仪器化测试 | AndroidJUnit | Room 迁移（v1→v2）、真实 CSV 导入 |
| OCR | 手工 | 10 张标准化验单，TSH/FT3/FT4/Tg 准确率 ≥ 70% |

---

## 10. 风险与决策

| 风险 | 缓解 |
|------|------|
| Flutter/Dart 代码不可复用 | 仅复用**数据契约**（CSV/WebDAV/参考范围），UI 与逻辑全重写 |
| 精确闹钟在 Android 12+ 需 `SCHEDULE_EXACT_ALARM` 权限且可能被系统限制 | 默认用 `WorkManager`(inexact)；用户授权精确闹钟后升级为 `AlarmManager` |
| OCR 依赖外部服务可用性 | 复用既有契约；增加健康探测与超时/重试；离线降级提示 |
| PDF 库许可 | 选 Apache-2.0 的 PdfBox-Android，规避 iText AGPL 限制 |
| Vico 对双轴/标注的支持成熟度 | 如需复杂标注，降级用 MPAndroidChart（经 `AndroidView` 嵌入 Compose） |
| 版本号随 Flutter 版演进 | 原生版独立版本线（建议从 `2.0.0` 起），`versionCode` 单独计数 |

---

## 11. 不变式（继承自 Flutter 版，须保持）

1. `LabRecord.id` 全局唯一。
2. `date` 不晚于当前日期。
3. 指标 `null` = 未检测，非 `0`。
4. `tg`（甲状腺球蛋白）与 `tg_lipid`（甘油三酯）永远独立，不可互换。
5. `MedicationChange.drug` 必为 `Drug` 枚举之一。
6. `custom_ref_ranges` 仅作用当前记录，不改全局默认。
7. `source` ∈ {manual, csv, ocr}；OCR 后修改仍记 `ocr`。
8. `updated_at ≥ created_at`，保存自动刷新。

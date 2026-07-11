# 保留数据模型（Room/Gson 序列化）
-keep class com.thytrack.android.domain.model.** { *; }
-keep class com.thytrack.android.data.local.room.** { *; }

# Gson / TypeConverter
-keepattributes *Annotation*
-keepattributes Signature

# OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# PdfBox-Android
-keep class org.apache.pdfbox.** { *; }
-dontwarn org.apache.pdfbox.**

# Vico
-dontwarn com.patrykandpatrick.vico.**

# Hilt
-keep class dagger.hilt.** { *; }

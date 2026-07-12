# 保留数据模型（Room/Gson 序列化）
-keep class com.thytrack.android.domain.model.** { *; }
-keep class com.thytrack.android.data.local.room.** { *; }

# 注解 / 泛型签名
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleFieldAnnotations

# OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# ML Kit（Phase 6.2）
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# AppCompat / core
-dontwarn androidx.appcompat.**
-dontwarn androidx.core.**

# Vico
-dontwarn com.patrykandpatrick.vico.**
-keep class com.patrykandpatrick.vico.** { *; }

# WorkManager
-dontwarn androidx.work.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepnames class * implements dagger.hilt.android.lifecycle.HiltViewModel
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Room / DataStore
-dontwarn androidx.room.**
-dontwarn androidx.datastore.**

# PdfBox-Android
-keep class org.apache.pdfbox.** { *; }
-dontwarn org.apache.pdfbox.**
-dontwarn org.bouncycastle.**


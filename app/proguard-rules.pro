# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ✅ AGGRESSIVE OPTIMIZATIONS FOR SMALLER BUNDLE SIZE
-optimizationpasses 8
-allowaccessmodification
-dontpreverify
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# ✅ KOTLIN MULTIPLATFORM & MODERN ANDROID RULES
# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes AnnotationDefault

# ✅ Keep classes with @Keep annotation
-keep class * {
    @androidx.annotation.Keep *;
}

# Hilt
-keep class com.jayma.pos.**_HiltModules { *; }
-keep class com.jayma.pos.**_HiltModules$* { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class com.jayma.pos.Hilt_JaymaApplication { *; }
-keep class com.jayma.pos.ui.Hilt_MainActivity { *; }
-keep class com.jayma.pos.ui.products.Hilt_ProductListFragment { *; }
-keep class com.jayma.pos.ui.setup.Hilt_PosSetupActivity { *; }
-keep class com.jayma.pos.ui.cart.Hilt_CartFragment { *; }
-keep class com.jayma.pos.ui.scanner.Hilt_BarcodeScannerFragment { *; }
-keep class com.jayma.pos.ui.reports.Hilt_SalesReportFragment { *; }
-keep class com.jayma.pos.ui.settings.Hilt_PrinterSettingsFragment { *; }
-keep class com.jayma.pos.ui.products.Hilt_ProductDetailFragment { *; }
-keep class com.jayma.pos.ui.cart.Hilt_CheckoutDialogFragment { *; }

# Keep all Hilt generated classes
-keep class **Hilt_* { *; }
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep interface <1>
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt - Keep all generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class com.jayma.pos.**_HiltModules { *; }
-keep class com.jayma.pos.**_HiltModules$* { *; }

# Keep ALL Hilt generated classes (critical for release builds)
-keep class com.jayma.pos.Hilt_* { *; }
-keep class com.jayma.pos.**.Hilt_* { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Application class and Hilt generated wrapper
-keep class com.jayma.pos.JaymaApplication { *; }
-keep class com.jayma.pos.Hilt_JaymaApplication { *; }

# Keep all Activities and Fragments with Hilt
-keep class com.jayma.pos.ui.** { *; }
-keep class com.jayma.pos.ui.**.Hilt_* { *; }

# Hilt entry points
-keepclassmembers class * {
    @dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper <init>(...);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep data classes for API models
-keep class com.jayma.pos.data.remote.models.** { *; }

# Keep entity classes
-keep class com.jayma.pos.data.local.entities.** { *; }

# Keep WorkManager workers
-keep class com.jayma.pos.sync.** { *; }

# Keep Logger utility
-keep class com.jayma.pos.util.Logger { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# WorkManager
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-dontwarn androidx.work.**

# ✅ Remove ALL logging in release builds (saves significant space)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ML Kit
-keep class com.google.mlkit.** { *; }

# CameraX
-keep class androidx.camera.** { *; }

# SUNMI Printer SDK
-keep class com.sunmi.printerx.** { *; }
-dontwarn com.sunmi.printerx.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ✅ Remove unused code and resources aggressively
-dontwarn **
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Data Binding - Keep all generated binding classes (MUST KEEP)
-keep class com.jayma.pos.databinding.ActivityMainBinding { *; }
-keep class com.jayma.pos.databinding.ActivityPosSetupBinding { *; }
-keep class com.jayma.pos.databinding.DialogCheckoutBinding { *; }
-keep class com.jayma.pos.databinding.FragmentBarcodeScannerBinding { *; }
-keep class com.jayma.pos.databinding.FragmentCartBinding { *; }
-keep class com.jayma.pos.databinding.FragmentPrinterSettingsBinding { *; }
-keep class com.jayma.pos.databinding.FragmentProductDetailBinding { *; }
-keep class com.jayma.pos.databinding.FragmentProductListBinding { *; }
-keep class com.jayma.pos.databinding.FragmentSalesReportBinding { *; }
-keep class com.jayma.pos.databinding.ItemCartBinding { *; }
-keep class com.jayma.pos.databinding.ItemProductBinding { *; }
-keep class com.jayma.pos.databinding.** { *; }
-keepclassmembers class * extends androidx.databinding.ViewDataBinding {
    <init>(...);
}

# Keep classes that reference generated classes
-keep class com.jayma.pos.JaymaApplication { *; }
-keep class com.jayma.pos.ui.MainActivity { *; }
-keep class com.jayma.pos.ui.setup.PosSetupActivity { *; }
-keep class com.jayma.pos.ui.cart.CartFragment { *; }
-keep class com.jayma.pos.ui.cart.CheckoutDialogFragment { *; }
-keep class com.jayma.pos.ui.products.ProductDetailFragment { *; }
-keep class com.jayma.pos.ui.products.ProductListFragment { *; }
-keep class com.jayma.pos.ui.reports.SalesReportFragment { *; }
-keep class com.jayma.pos.ui.scanner.BarcodeScannerFragment { *; }
-keep class com.jayma.pos.ui.settings.PrinterSettingsFragment { *; }
-keep class com.jayma.pos.ui.adapter.CartAdapter { *; }
-keep class com.jayma.pos.ui.adapter.ProductAdapter { *; }

# Add project specific ProGuard rules here.
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

# Preserve ViewModel classes and their constructors
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    public <init>(android.app.Application);
}

# Preserve Data Models (for GSON serialization)
-keep class ua.entaytion.simi.data.model.** { *; }
-keep class ua.entaytion.simi.viewmodel.** { *; }
-keepattributes Signature, EnclosingMethod, InnerClasses
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Preserve Workers
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
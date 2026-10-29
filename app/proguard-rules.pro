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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Couchbase Lite: keep API + JNI-related classes to avoid JNI_OnLoad failures in minified builds.
-keep class com.couchbase.lite.** { *; }
-keep class com.couchbase.lite.internal.** { *; }
-keep class com.couchbase.litecore.** { *; }
-dontwarn com.couchbase.lite.**
-dontwarn com.couchbase.litecore.**

# App Couchbase models use Kotlin reflection (memberProperties/constructor parameter names).
# Keep names and metadata to avoid release-only query mismatches (e.g., "category" key lookups).
-keep class es.sebas1705.couchbase.documents.** { *; }
-keep class es.sebas1705.couchbase.documents.abstracts.** { *; }
-keep class es.sebas1705.couchbase.datasources.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,MethodParameters


# Strip verbose/debug/info logging from release builds so nothing logged at those levels
# (user data, tokens, debug payloads) ever reaches logcat. Warnings and errors are kept.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

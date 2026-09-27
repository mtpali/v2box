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

# Gson reads and writes configs, subscriptions, and profiles by field name.
# Preserve serialized field names while allowing R8 to rename classes.
-keepclassmembers class com.v2box.mobiletina.dto.** {
    <fields>;
}
-keepattributes Signature,InnerClasses,EnclosingMethod

# The tunnel library resolves these exact JVM class/method names through JNI.
-keep class com.v2box.mobiletina.service.TProxyService { *; }
-keep class com.v2box.mobiletina.service.TProxyService$Companion { *; }
# gomobile's native bridge resolves its generated libv2ray wrapper classes by name.
-keep class libv2ray.** { *; }

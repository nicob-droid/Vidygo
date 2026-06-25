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

# Preserve file/line info so Play Console and Crashlytics show readable traces after deobfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ===== ANDROIDX ROOM DATABASE =====
# Room uses runtime reflection for DAO generation
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** getInstance(...);
}

# Keep all Entity and DAO classes
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * {
    <methods>;
}

# Keep Room annotations
-keep class androidx.room.** { *; }
-keepclassmembers class androidx.room.** {
    public *;
}

# ===== ANDROIDX WORKMANAGER =====
# WorkManager uses runtime reflection to instantiate workers
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep interface androidx.work.** { *; }

# Keep Worker implementations
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# ===== ANDROIDX STARTUP =====
# Keep Initializer implementations
-keep class androidx.startup.** { *; }
-keep class * implements androidx.startup.Initializer { *; }

# ===== ANDROID SQLITE =====
# Keep SQLite (used by Room)
-keep class android.database.sqlite.** { *; }

# ===== JSON (org.json) =====
# Keep JSON processing
-keep class org.json.** { *; }
-keepclassmembers class org.json.** {
    public *;
}

# ===== GOOGLE PLAY SERVICES (AdMob, GMS) =====
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keepclassmembers class com.google.android.gms.ads.** {
    public *;
}

# ===== GLIDE IMAGE LOADING =====
-keep class com.bumptech.glide.** { *; }
-keep class * implements com.bumptech.glide.module.GlideModule
-keepclassmembers class com.bumptech.glide.** {
    public *;
}

# ===== GENERIC RULES =====
# Keep model classes (Video, Channel, etc.)
-keep class io.github.nicobdroid.vidygo.model.** { *; }

# Keep Activity/Fragment subclasses
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


# ProGuard Rules for Voice Notes Android App
# Optimized for release builds with minification and obfuscation

# ============================================
# General Android Configuration
# ============================================

# Keep line numbers for debugging release builds
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views and their constructors
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep setters in Views for animations
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep Activity methods for reflection
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================
# Room Database
# ============================================

# Keep Room entities and their fields
-keep @androidx.room.Entity class * {
    *;
}

# Keep DAO interfaces and classes
-keep @androidx.room.Dao class * {
    *;
}
-keep interface * extends androidx.room.Dao {
    *;
}

# Keep Database class
-keep @androidx.room.Database class * {
    *;
}

# Keep Room type converters
-keep class * {
    @androidx.room.TypeConverter <methods>;
}

# Keep Room migration classes
-keep class * extends androidx.room.migration.Migration {
    *;
}

# Keep all Room annotations
-keepattributes *Annotation*
-keep class androidx.room.** { *; }

# ============================================
# Kotlin Configuration
# ============================================

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*, Signature, Exception

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin reflection
-keep class kotlin.Metadata { *; }

# Keep data classes
-keepclassmembers class * {
    *** component1();
    *** component2();
    *** component3();
    *** component4();
    *** component5();
    *** copy(...);
}

# ============================================
# Google Cloud Speech-to-Text
# ============================================

# Keep Google Cloud libraries
-keep class com.google.cloud.** { *; }
-dontwarn com.google.cloud.**

# Keep gRPC
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# Keep Protobuf
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# Keep Google API client
-keep class com.google.api.** { *; }
-dontwarn com.google.api.**

# Keep Google Auth
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**

# ============================================
# OkHttp
# ============================================

# Keep OkHttp platform classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Okio
-keep class okio.** { *; }
-dontwarn okio.**

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================
# Google Play Services
# ============================================

# Keep Google Play Services Location
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep location APIs
-keep class com.google.android.gms.location.** { *; }

# ============================================
# AndroidX and Material Design
# ============================================

# Keep AndroidX libraries
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}

# ============================================
# Application-Specific Rules
# ============================================

# Keep BuildConfig
-keep class com.voicenotes.motorcycle.BuildConfig { *; }

# Keep main application class
-keep public class com.voicenotes.motorcycle.** {
    public protected *;
}

# Keep Service classes
-keep class * extends android.app.Service {
    public protected *;
}

# Keep BroadcastReceiver classes
-keep class * extends android.content.BroadcastReceiver {
    public protected *;
}

# Keep database package
-keep class com.voicenotes.motorcycle.database.** {
    *;
}

# Keep V2SStatus enum explicitly
-keep enum com.voicenotes.motorcycle.database.V2SStatus {
    *;
}

# Keep Recording entity explicitly
-keep class com.voicenotes.motorcycle.database.Recording {
    *;
}

# Keep DAO explicitly
-keep interface com.voicenotes.motorcycle.database.RecordingDao {
    *;
}

# Keep TranscriptionService
-keep class com.voicenotes.motorcycle.TranscriptionService {
    public protected *;
}

# Keep OverlayService
-keep class com.voicenotes.motorcycle.OverlayService {
    public protected *;
}

# Keep BatchProcessingService
-keep class com.voicenotes.motorcycle.BatchProcessingService {
    public protected *;
}

# ============================================
# Optimization Configuration
# ============================================

# R8 automatically optimizes with selective exclusions
# No additional configuration needed - R8 is more efficient than classic ProGuard

# ============================================
# Debugging (remove in final production)
# ============================================

# Print mapping to understand obfuscation
-printmapping build/outputs/mapping/release/mapping.txt

# Print usage for dead code analysis
-printusage build/outputs/mapping/release/usage.txt

# Print seeds (classes that were kept)
-printseeds build/outputs/mapping/release/seeds.txt

# ============================================
# Warnings to Suppress
# ============================================

# Suppress warnings for missing classes
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn org.codehaus.mojo.**
-dontwarn com.google.errorprone.**
-dontwarn javax.naming.**

# Ignore warnings about missing Google Play Services classes
-dontwarn com.google.android.gms.**

# Ignore warnings about reflection in libraries
-dontwarn java.lang.invoke.StringConcatFactory

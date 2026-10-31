# ProGuard rules for FitLog Pro
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

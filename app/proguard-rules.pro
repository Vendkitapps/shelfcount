# ML Kit creates its component registrars by reflection at startup (MlKitInitProvider).
# R8 full mode removes their no-argument constructors, so barcode scanning silently
# stops working in release builds ("Could not instantiate ...Registrar"). Keep them.
-keep class com.google.mlkit.**.*Registrar { <init>(); }
-keepclassmembers class * implements com.google.firebase.components.ComponentRegistrar { public <init>(); }

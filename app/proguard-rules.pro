# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Appli\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# on ignore les warnings concernant le client HTTP Apache qui a été supprimé dans l'API 23
# La librairie Android Volley est toujours compilé avec l'API 22 et on utilise OKHTTP comme client
-dontwarn com.android.volley.toolbox.**

# configuration pour utiliser la librairie OKHTTP
-dontwarn okio.**
-dontwarn com.squareup.okhttp.internal.huc.**

# configuration pour utiliser la librairie spongycastle
-keep class org.spongycastle.**
-dontwarn org.spongycastle.jce.provider.X509LDAPCertStoreSpi
-dontwarn org.spongycastle.x509.util.LDAPStoreHelper
-dontwarn javax.naming.**

# android support
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

# RxJava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}


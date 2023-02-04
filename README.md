# ImGui Android standalone Implementation
This project implements ImGui C++ on Android with GLSurfaceView, Has AssetsManager accessibility, Shows The ImGui Windows as System Overlay. You can change it to different showing implementation as well.


# NOTE!
This project fully experimental. before you complain about bugs, try to fix it yourself first!

## In your game MainActivity
```smali
# onCreate
invoke-static {p0}, Lakn/main/Main;->Start(Landroid/content/Context;)V
```
## `AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application>
        ...
    <service
        android:name="akn.main.Service"
        android:enabled="true"
        android:exported="false"
        android:stopWithTask="true" />
        ...
    </application>
</manifest>
```


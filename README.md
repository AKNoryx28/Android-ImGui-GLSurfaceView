# ImGui Android standalone Implementation
This project implements ImGui C++ on Android with GLSurfaceView, Has AssetsManager accessibility, Shows The ImGui Windows as System Overlay. You can change it to different showing implementation as well.


# NOTE!
This project fully experimental. before you complain about bugs, try to fix it yourself first!

## In your game MainActivity
```smali
# onCreate
invoke-static {p0}, Lakn/main/Main;->RequestOverlayPermission(Landroid/app/Activity;)V
invoke-static {p0}, Lakn/main/Main;->Start(Landroid/content/Context;)V

# onPause
const/4 v0, 0x0
invoke-static {v0}, Lakn/main/Main;->setVisible(Z)V

# onResume
const/4 v0, 0x1
invoke-static {v0}, Lakn/main/Main;->setVisible(Z)V

# onDestroy
invoke-static {}, Lakn/main/ImGuiSurface;->tryShutdown()V
```


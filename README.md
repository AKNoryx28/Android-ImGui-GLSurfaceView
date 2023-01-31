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

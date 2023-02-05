package akn.main;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


public class Service extends android.app.Service {

    static final int MAX_WINDOW_AMOUNT = 20;

    static {
        System.loadLibrary("GPX");
    }

    int[] windowRegistered;
    WindowManager.LayoutParams[] touchHandlerParams;
    View[] touchHandlers;
    ImGuiSurface imGuiSurface;
    WindowManager windowManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Start();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void Start() {
        imGuiSurface = new ImGuiSurface(this);

        touchHandlerParams = new WindowManager.LayoutParams[MAX_WINDOW_AMOUNT];
        windowRegistered = new int[MAX_WINDOW_AMOUNT];
        touchHandlers = new View[MAX_WINDOW_AMOUNT];

        for (int i = 0; i < MAX_WINDOW_AMOUNT; i++) {
            windowRegistered[i] = 0;
            touchHandlerParams[i] = null;
            touchHandlers[i] = null;
        }

        WindowManager.LayoutParams params = getParams(true);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(imGuiSurface, params);

        final Handler handlerUpdateTouchView = new Handler();
        handlerUpdateTouchView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ImGuiSurface.Initialized()) {
                    try {
                        String[] winds = ImGuiSurface.GetWindowsTracked();
                        for (int i = 0; i < winds.length; i++) {
                            String[] win_idposxy = winds[i].split("\\|");
                            if (win_idposxy[0].equals("1000")) {
                                if (touchHandlers[i] != null && windowRegistered[i] != 0 && touchHandlerParams[i] != null) {
                                    windowManager.removeView(touchHandlers[i]);
                                    windowRegistered[i] = 0;
                                    touchHandlers[i] = null;
                                    touchHandlerParams[i] = null;
                                }
                                continue;
                            }

                            int id = Integer.parseInt(win_idposxy[0]);

                            if (windowRegistered[i] != id && touchHandlers[i] == null && touchHandlerParams[i] == null) {
                                View view = new View(Service.this);

                                // FIXME: This color used for debug, I'm not pretty sure if tracked correctly
                                view.setBackgroundColor(Color.BLACK);
                                view.setAlpha(0.05f);

                                view.setOnTouchListener(new View.OnTouchListener() {
                                    @SuppressLint("ClickableViewAccessibility")
                                    @Override
                                    public boolean onTouch(View v, MotionEvent e) {
                                        int action = e.getAction();
                                        switch (action) {
                                            case MotionEvent.ACTION_MOVE:
                                            case MotionEvent.ACTION_DOWN:
                                            case MotionEvent.ACTION_UP:
                                                ImGuiSurface.OnTouch(action != MotionEvent.ACTION_UP, e.getRawX(), e.getRawY());
                                                break;
                                            default:
                                                break;
                                        }
                                        return false;
                                    }
                                });
                                windowRegistered[i] = id;
                                touchHandlerParams[i] = getParams(false);
                                touchHandlers[i] = view;
                                windowManager.addView(touchHandlers[i], touchHandlerParams[i]);
                                continue;
                            }
                            if (touchHandlers[i] != null && touchHandlers[i].getVisibility() != View.INVISIBLE) {
                                touchHandlerParams[i].x = (int) Float.parseFloat(win_idposxy[1]);
                                touchHandlerParams[i].y = (int) Float.parseFloat(win_idposxy[2]);
                                touchHandlerParams[i].width = (int) Float.parseFloat(win_idposxy[3]);
                                touchHandlerParams[i].height = (int) Float.parseFloat(win_idposxy[4]);
                                windowManager.updateViewLayout(touchHandlers[i], touchHandlerParams[i]);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handlerUpdateTouchView.postDelayed(this, 100);
            }
        }, 500);
    }

    public WindowManager.LayoutParams getParams(boolean window) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR;
        }

        if (window) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        params.format = PixelFormat.RGBA_8888;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = params.y = 0;
        params.width = params.height = window ? WindowManager.LayoutParams.MATCH_PARENT : 0;
        return params;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return android.app.Service.START_NOT_STICKY;
    }

    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    public void onDestroy() {
        super.onDestroy();
        if (imGuiSurface != null) {
            for (View v : touchHandlers)
                if (v != null) windowManager.removeView(v);
            windowManager.removeView(imGuiSurface);
        }
    }

    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    private void Thread() {
        if (imGuiSurface == null) {
            return;
        }
        if (isNotInGame()) {
            for (View v : touchHandlers)
                if (v != null) v.setVisibility(View.INVISIBLE);
            imGuiSurface.setVisibility(View.INVISIBLE);
        } else {
            for (View v : touchHandlers)
                if (v != null) v.setVisibility(View.VISIBLE);
            imGuiSurface.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


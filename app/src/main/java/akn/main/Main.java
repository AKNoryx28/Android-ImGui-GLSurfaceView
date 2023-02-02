package akn.main;

import static android.view.View.GONE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Main {
    static final String LOG_TAG = "GPX";

    public static WindowManager mWm;
    static ImGuiSurface imGuiSurface;

    static int[] windowRegistered;
    static WindowManager.LayoutParams[] list_vParams;
    static View[] touchHandlers;
    static final int MAX_WINDOW_AMOUNT = 20;
    static int WINDOW_AMOUNT = 0;

    @SuppressLint("ClickableViewAccessibility")
    public static void Start(Context context) {
        System.loadLibrary("GPX");
        if (!FloatingAllowed) {
            Toast.makeText(context, "Overlay permission required！", Toast.LENGTH_LONG).show();
            return;
        }
        mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams wParams = getParams(true);

        imGuiSurface = new ImGuiSurface(context);

        windowRegistered = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        list_vParams = new WindowManager.LayoutParams[MAX_WINDOW_AMOUNT];
        touchHandlers = new View[MAX_WINDOW_AMOUNT];

        mWm.addView(imGuiSurface, wParams);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ImGuiSurface.Initialized()) return;
                try {
                    String[] winds = ImGuiSurface.GetWindowsTracked();
                    WINDOW_AMOUNT = winds.length;
                    for (int i = 0; i < winds.length; i++) {
                        String[] win_idposxy = winds[i].split("\\|");
                        if (win_idposxy[0].isEmpty()) {
                            if (touchHandlers[i] != null) {
                                mWm.removeView(touchHandlers[i]);
                                touchHandlers[i] = null;
                            }
                            continue;
                        }
                        int id = Integer.parseInt(win_idposxy[0]);
                        if (windowRegistered[i] != id || touchHandlers[i] == null) {
                            View view = new View(context);
                            view.setOnTouchListener((v, e) -> {
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
                            });
                            WindowManager.LayoutParams params = getParams(false);
                            windowRegistered[i] = id;
                            list_vParams[i] = params;
                            touchHandlers[i] = view;
                            mWm.addView(touchHandlers[i], list_vParams[i]);
                            continue;
                        }
                        list_vParams[i].x = (int) Float.parseFloat(win_idposxy[1]);
                        list_vParams[i].y = (int) Float.parseFloat(win_idposxy[2]);
                        list_vParams[i].width = (int) Float.parseFloat(win_idposxy[3]);
                        list_vParams[i].height = (int) Float.parseFloat(win_idposxy[4]);
                        mWm.updateViewLayout(touchHandlers[i], list_vParams[i]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, 700);
            }
        },700);
    }

    public static WindowManager.LayoutParams getParams(boolean window) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
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

    public static void setVisible(boolean pState) {
        if (!ImGuiSurface.Initialized())
            return;
        if (pState)
            imGuiSurface.setVisibility(View.VISIBLE);
        else
            imGuiSurface.setVisibility(GONE);
    }

    public static boolean FloatingAllowed = false;
    public static void RequestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(activity)) {
                FloatingAllowed = true;
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+activity.getPackageName()));
                activity.startActivityForResult(intent, 5004);
            }
        }
    }
}

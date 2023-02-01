package akn.main;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Main {
    public static WindowManager mWm;
    public static WindowManager.LayoutParams mWmParams;
    static ImGuiSurface imGuiSurface;

    @SuppressLint("ClickableViewAccessibility")
    public static void Start(Context context) {
        System.loadLibrary("GPX");
        if (!FloatingAllowed) {
            Toast.makeText(context, "Overlay permission required！", Toast.LENGTH_LONG).show();
            return;
        }

        mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWmParams = getParams(false);
        WindowManager.LayoutParams wParams = getParams(true);
        imGuiSurface = new ImGuiSurface(context);
        View mTouch = new View(context);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.BLACK);
        gd.setStroke(2,Color.RED);
        mTouch.setBackground(gd);

        mWm.addView(imGuiSurface, wParams);
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

//        if (window) {
//            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//        }
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

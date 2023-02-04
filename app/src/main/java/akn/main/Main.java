package akn.main;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.WindowManager;

class Main {
    public static WindowManager wm;
    public static void Start(final Context context) {
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (wm != null)
                    context.startService(new Intent(context, Service.class));
                else handler.postDelayed(this,1000);
            }
        },1000);
    }
}
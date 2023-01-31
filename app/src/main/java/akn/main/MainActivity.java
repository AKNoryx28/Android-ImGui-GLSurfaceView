package akn.main;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main.RequestOverlayPermission(this);
        setContentView(R.layout.activity_main);
        Main.Start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Main.setVisible(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Main.setVisible(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImGuiSurface.tryShutdown();
    }
}
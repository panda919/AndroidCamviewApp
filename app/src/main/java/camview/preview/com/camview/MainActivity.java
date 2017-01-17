package camview.preview.com.camview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent s = new Intent(MainActivity.this, FloatingCamera_Back.class);
        s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(s);
        finish();
    }
}

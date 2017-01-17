package camview.preview.com.camview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SwitchCamera extends Activity{

    @Override
    protected void onCreate(Bundle Saved){
        super.onCreate(Saved);

        Intent s = getIntent();
        final String sw = s.getStringExtra("sw");		System.out.println(sw);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sw.contains("toFront")){
                    System.out.println("Start Front Camera");

                    Intent front = new Intent(SwitchCamera.this, FloatingCamera_Front.class);
                    front.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(front);

                    finish();
                }
                else if(sw.contains("toBack")){
                    System.out.println("Start Back Camera");

                    Intent back = new Intent(SwitchCamera.this, FloatingCamera_Back.class);
                    back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(back);

                    finish();
                }
            }
        }, 100);
    }
}

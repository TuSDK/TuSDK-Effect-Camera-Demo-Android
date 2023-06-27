package org.lasque.effectcamerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tusdk.pulse.Engine;
import com.tusdk.pulse.filter.TuSDKFilter;

import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper;

/**
 * 启动页
 */
public class SplashActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String versionCode = "© TUTUCLOUD.COM \n" + TuSDKFilter.BUILD_VERSION;
        ((TextView) findViewById(R.id.lsq_version_code)).setText(versionCode);

//        ((TextView) findViewById(R.id.lsq_version_code)).setOnClickListener(new TuSdkViewHelper.OnSafeClickListener() {
//            @Override
//            public void onSafeClick(View v) {
//                startActivity(new Intent(SplashActivity.this, MovieRecordFullScreenActivity.class));
//                overridePendingTransition(R.anim.lsq_fade_in,R.anim.lsq_fade_out);
//            }
//        });

        Engine engine = Engine.getInstance();
        engine.init(null);


        ThreadHelper.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MovieRecordFullScreenActivity.class));
                overridePendingTransition(R.anim.lsq_fade_in,R.anim.lsq_fade_out);
                finish();
            }
        },2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Engine.getInstance().release();
    }
}

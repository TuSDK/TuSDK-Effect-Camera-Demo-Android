package org.lasque.effectcamerademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.tusdk.pulse.Engine;

import org.lasque.effectcamerademo.utils.PermissionUtils;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.secret.StatisticsManger;
import org.lasque.tusdkpulse.core.utils.ContextUtils;
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper;
import org.lasque.tusdkpulse.modules.components.ComponentActType;

/**
 * 首页界面
 */
public class DemoEntryActivity extends FragmentActivity {

    /** 布局ID */
    public static final int layoutId = R.layout.demo_entry_activity;

    /** 1为录制  2为编辑  3功能列表 **/
    private int mRequestCode = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // sdk统计代码，请不要加入您的应用
        StatisticsManger.appendComponent(ComponentActType.sdkComponent);

        setContentView(layoutId);

        ImageButton menuButton = (ImageButton)findViewById(R.id.lsq_app_menu);
        menuButton.setOnClickListener(mClickListener);

        RadioButton recordButton = (RadioButton)findViewById(R.id.lsq_app_record);
        recordButton.setOnClickListener(mClickListener);

        RadioButton clipButton = (RadioButton)findViewById(R.id.lsq_app_clip);
        clipButton.setOnClickListener(mClickListener);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Engine.getInstance().release();
    }

    /**
     * 点击事件监听
     */
    private View.OnClickListener mClickListener = new TuSdkViewHelper.OnSafeClickListener() {
        @Override
        public void onSafeClick(View v) {
            switch (v.getId()){
                case R.id.lsq_app_menu:
                    break;
                case R.id.lsq_app_record:
                    handleRecordButton();
                    break;
                case R.id.lsq_app_clip:
                    break;
            }
        }
    };

    /**
     * 处理编辑视频按钮操作
     */
    private void handleEditorButton()
    {
        mRequestCode = 2;

        if (PermissionUtils.hasRequiredPermissions(this, getRequiredPermissions()))
        {

        }
        else
        {
            PermissionUtils.requestRequiredPermissions(this, getRequiredPermissions());
        }

    }

    /**
     * 开启录制相机
     */
    private void handleRecordButton()
    {

        mRequestCode = 1;
        if (PermissionUtils.hasRequiredPermissions(this, getRequiredPermissions()))
        {
            Intent intent = new Intent(this, MovieRecordFullScreenActivity.class);
            this.startActivity(intent);
        }
        else
        {
            PermissionUtils.requestRequiredPermissions(this, getRequiredPermissions());
        }
    }

    /**
     * 组件运行需要的权限列表
     *
     * @return
     *            列表数组
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected String[] getRequiredPermissions()
    {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE
        };

        return permissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.handleRequestPermissionsResult(requestCode, permissions, grantResults, this, mGrantedResultDelgate);
    }

    /**
     * 授予权限的结果，在对话结束后调用
     *
     * @param permissionGranted
     *            true or false, 用户是否授予相应权限
     */
    protected PermissionUtils.GrantedResultDelgate mGrantedResultDelgate = new PermissionUtils.GrantedResultDelgate()
    {
        @Override
        public void onPermissionGrantedResult(boolean permissionGranted)
        {
            if (permissionGranted)
            {
                if(mRequestCode == 1) {
                    Intent intent = new Intent(DemoEntryActivity.this, MovieRecordFullScreenActivity.class);
                    DemoEntryActivity.this.startActivity(intent);
                }
            }
            else
            {
                String msg = TuSdkContext.getString("lsq_camera_no_access", ContextUtils.getAppName(DemoEntryActivity.this));

                TuSdkViewHelper.alert(permissionAlertDelegate, DemoEntryActivity.this, TuSdkContext.getString("lsq_camera_alert_title"),
                        msg, TuSdkContext.getString("lsq_button_close"), TuSdkContext.getString("lsq_button_setting")
                );
            }
        }
    };

    /**
     * 权限警告提示框点击事件回调
     */
    protected TuSdkViewHelper.AlertDelegate permissionAlertDelegate = new TuSdkViewHelper.AlertDelegate()
    {
        @Override
        public void onAlertConfirm(AlertDialog dialog)
        {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", DemoEntryActivity.this.getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @Override
        public void onAlertCancel(AlertDialog dialog)
        {

        }
    };
}

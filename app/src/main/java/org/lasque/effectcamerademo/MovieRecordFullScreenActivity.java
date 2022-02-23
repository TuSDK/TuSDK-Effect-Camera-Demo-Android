package org.lasque.effectcamerademo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.SizeF;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.tusdk.pulse.Engine;
import com.tusdk.pulse.MediaInspector;
import com.tusdk.pulse.filter.Image;

import org.lasque.effectcamerademo.album.AlbumActivity;
import org.lasque.effectcamerademo.album.AlbumInfo;
import org.lasque.effectcamerademo.audio.AudioItem;
import org.lasque.effectcamerademo.audio.AudioListActivity;
import org.lasque.effectcamerademo.audio.AudioMixerItem;
import org.lasque.effectcamerademo.audio.AudioRecord;
import org.lasque.effectcamerademo.utils.Constants;
import org.lasque.effectcamerademo.utils.PermissionUtils;
import org.lasque.effectcamerademo.utils.SensorHelper;
import org.lasque.effectcamerademo.views.record.RecordView;
import org.lasque.tubeautysetting.AudioConvert;
import org.lasque.tubeautysetting.PipeMediator;
import org.lasque.tubeautysetting.RecordManager;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.TuSdkResult;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.TuSdkSizeF;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.ContextUtils;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdkpulse.core.utils.image.BitmapHelper;
import org.lasque.tusdkpulse.core.utils.image.ImageOrientation;
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCameraShot;
import org.lasque.tusdkpulse.cx.hardware.camera.impl.TuCameraImpl;
import org.lasque.tusdkpulse.cx.hardware.utils.TuCameraAspectRatio;
import org.lasque.tusdkpulse.cx.seles.extend.TuSurfaceTextureHolder;

import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.DIRECTORY_DCIM;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Normal;
import static org.lasque.effectcamerademo.base.BaseActivity.ALBUM_REQUEST_CODE;
import static org.lasque.effectcamerademo.base.BaseActivity.ALBUM_RESULT_CODE;
import static org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs.CameraState.START;

/**
 * TuSDK
 * org.lasque.effectcamerademo
 * ECDemo
 *
 * @author H.ys
 * @Date 2021/1/20  14:51
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class MovieRecordFullScreenActivity extends FragmentActivity {

    public class VideoFragmentItem{
        public String path;
        public long fragmentDuration;
    }

    private static long CURRENT_MAX_RECORD_DURATION = 60000;

    private static long MAX_RECORD_DURATION = 60000;

    private static int AUDIO_REQUEST_CODE = 4;

    private static double PROCESS_WIDTH = 720;

    private static TuSdkSize PREVIEW_SIZE = new TuSdkSize(1920,1080);

    private FrameLayout mCameraView;

    private RecordView mRecordView;

    private TuCamera mCamera;

    protected TuSdkSize mInputSize;

    protected TuSdkSize mCurrentRenderSize;

    private ImageOrientation mPreviewOrientation;

    /** SurfaceTexture */
    private SurfaceTexture mSurfaceTexture;

    private boolean isInitSuccess = false;

    private boolean isRecording = false;

    private double mCurrentStretch = 1.0;

    private double mCurrentVideoStretch = 1.0;

    private Bitmap mShotPhoto;

    private ImageView mVideoSelectView;


    // ----------------------- 音频录制 ------------------------- //
    private int mBufferSize = 0;

    private int mBlockByteLength = 0;

    private long mRecordingStart = 0;

    private int mCurrentAudioEffect = PITCH_TYPE_Normal;

    private List<VideoFragmentItem> mVideoLists = new ArrayList<>();

    private long mCurrentDuration = 0L;

    private long mCurrentFragmentDuration = 0L;

    private VideoFragmentItem mCurrentFragment;

    private TuCameraAspectRatio mCurrentRatio;

    private TuCameraAspectRatio mRectRatio;

    private TuSdkSize mRectSize;

    private String mCurrentDoubleViewVideoPath = "";

    private String mCurrentAudioPath = "";

    private RecordView.DoubleViewMode mCurrentMode;

    private AudioMixerItem mAudioItem;

    private long mCurrentPlayerPos = 0;


    //------------------------------------------ PipeMediator ---------------------------------------- //

    private PipeMediator mPipeMediator;

    private AudioRecord mAudioRecord;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALBUM_REQUEST_CODE){
            if (resultCode == ALBUM_RESULT_CODE){

                TuSdkSize size = ViewSize.create(mCameraView);
                mCurrentRatio = TuCameraAspectRatio.of(size.width,size.height);
                mRectSize = TuSdkSize.create((int) PROCESS_WIDTH, (int) (PROCESS_WIDTH / mCurrentRatio.getX() * mCurrentRatio.getY()));
                mCurrentRenderSize = mRectSize;
                mPipeMediator.updateAspect(new SizeF(mCurrentRatio.getX(),mCurrentRatio.getY()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordView.onDoubleView();
                    }
                });

                Bundle bundle = data.getBundleExtra("select");
                List<AlbumInfo> infos = (ArrayList<AlbumInfo>) bundle.getSerializable("select");
                if (infos.isEmpty()) return;


                AlbumInfo info = infos.get(0);
                CURRENT_MAX_RECORD_DURATION = info.getDuration();
                MAX_RECORD_DURATION = info.getDuration();
                mCurrentDoubleViewVideoPath = info.getPath();
                updateDoubleView(info.getPath(),info.getAudioPath());

                mCurrentAudioPath = info.getAudioPath();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordView.setAudioName("选择音乐");

                        mRecordView.updateMinPosition(((float) Constants.MIN_RECORDING_TIME) / (CURRENT_MAX_RECORD_DURATION / 1000));
                    }
                });


            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordView.switchCameraModeButton(RecordView.RecordType.SHORT_CLICK_RECORD);
                    }
                });
            }
        } else if (requestCode == AUDIO_REQUEST_CODE){
            if (requestCode == AUDIO_REQUEST_CODE && resultCode == 11){
                AudioItem audioPath = ((AudioItem) data.getSerializableExtra("audioPath"));

                CURRENT_MAX_RECORD_DURATION = 15000;
                MAX_RECORD_DURATION = 15000;
                mCurrentAudioPath = audioPath.getPath();

                mPipeMediator.setBGM(mCurrentAudioPath,0L);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(audioPath.getPath())){
                            mRecordView.setAudioName("选择音乐");
                        } else {
                            mRecordView.setAudioName(audioPath.getName());
                        }

                        mRecordView.updateMinPosition(((float) Constants.MIN_RECORDING_TIME) / (CURRENT_MAX_RECORD_DURATION / 1000.f));

                    }
                });

            }
        }

    }


    /**
     * @param path
     */
    private void updateDoubleView(String path,String audioPath) {

        RectF cameraRect = new RectF(0,0,1,1);
        RectF videoRect = new RectF(0,0,1,1);

        TuSdkSize videoSize = new  TuSdkSize();
        long videoDuration = 0;

        MediaInspector.MediaInfo mediaInfo = MediaInspector.shared().inspect(path);
        for (MediaInspector.MediaInfo.AVItem item : mediaInfo.streams){
            if (item instanceof MediaInspector.MediaInfo.Video){
                videoSize.width = ((MediaInspector.MediaInfo.Video) item).width;
                videoSize.height = ((MediaInspector.MediaInfo.Video) item).height;
                videoDuration = item.duration;
                break;
            }
        }
        TuSdkSizeF videoSizePercent = new TuSdkSizeF();

        switch (mRecordView.getCurrentDoubleViewMode()){
            case None:
                return;
            case ViewInView:
                videoRect.top = 0.1f;
                videoRect.left = 0.1f;
                if (videoSize.width > videoSize.height){
                    float width = 0.5f;
                    float height = mCurrentRenderSize.width * width * videoSize.minMaxRatio() / mCurrentRenderSize.height;
                    videoRect.right = videoRect.left + width;
                    videoRect.bottom = videoRect.top + height;
                } else {
                    float height = 0.3f;
                    float width = mCurrentRenderSize.height * height * videoSize.minMaxRatio() / mCurrentRenderSize.width;
                    videoRect.bottom = videoRect.top + height;
                    videoRect.right = videoRect.left + width;
                }

                break;
            case TopBottom:
                int halfHeight = mCurrentRenderSize.height / 2;
                cameraRect.left = 0.25f;
                cameraRect.right = 0.75f;
                cameraRect.top = 0.5f;
                cameraRect.bottom = 1.0f;

                if (videoSize.width > videoSize.height){
                    float renderWidth = mCurrentRenderSize.width;
                    float heightPercent = renderWidth * videoSize.minMaxRatio() / mCurrentRenderSize.height;

                    videoRect.left = 0f;
                    videoRect.right = 1f;
                    videoRect.top = (0.5f - heightPercent) / 2f;
                    videoRect.bottom = videoRect.top + heightPercent;


                } else {
                    float widthPercent = halfHeight * videoSize.minMaxRatio() / mCurrentRenderSize.width;

                    videoRect.left = (1 - widthPercent) / 2f;
                    videoRect.right = videoRect.left + widthPercent;

                    videoRect.top = 0.0f;
                    videoRect.bottom = 0.5f;

                }
                break;
            case LeftRight:
                cameraRect.left = 0f;
                cameraRect.right = 0.5f;
                cameraRect.top = 0.25f;
                cameraRect.bottom = 0.75f;

                if (videoSize.width > videoSize.height){
                    float renderWidth = mCurrentRenderSize.width / 2f;
                    float heightPercent = renderWidth * videoSize.minMaxRatio() / mCurrentRenderSize.height;

                    videoRect.left = 0.5f;
                    videoRect.right = 1.0f;
                    videoRect.top = (1f - heightPercent) / 2f;
                    videoRect.bottom = videoRect.top + heightPercent;


                } else {
                    videoRect.left = 0.5f;
                    videoRect.right = 1f;

                    int halfWidth = mCurrentRenderSize.width / 2;
                    float heightPercent = halfWidth * videoSize.maxMinRatio() / mCurrentRenderSize.height;


                    videoRect.top = (1 - heightPercent) / 2;
                    videoRect.bottom = videoRect.top + heightPercent;

                }
                break;
        }


        long finalVideoDuration = videoDuration;

        mPipeMediator.setJoiner(path,audioPath,cameraRect,videoRect,0L);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mVideoSelectView == null){
                    mVideoSelectView = new ImageView(MovieRecordFullScreenActivity.this);
                    mVideoSelectView.setImageResource(R.drawable.rhythm_ic_pic);
                    mVideoSelectView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openAlbum();
                        }
                    });
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ContextUtils.dip2px(TuSdkContext.context(),24),ContextUtils.dip2px(TuSdkContext.context(),24));
                    mRecordView.addView(mVideoSelectView,layoutParams);
                }
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoSelectView.getLayoutParams();

                int parentWidth = mRecordView.getWidth();
                int parentHeight = mRecordView.getHeight();

                int renderWidth = parentWidth;
                int renderHeight = parentHeight;

                int offsetY = 0;

                float x = 0;
                float y = 0;

                if (mCurrentDuration>0){
                    mVideoSelectView.setVisibility(View.GONE);
                } else {
                    mVideoSelectView.setVisibility(View.VISIBLE);
                }


                switch (mRecordView.getCurrentDoubleViewMode()){

                    case None:
                        mVideoSelectView.setVisibility(View.GONE);
                        break;
                    case ViewInView:
                        x =  (parentWidth * videoRect.left + ContextUtils.dip2px(TuSdkContext.context(),8));
                        y = offsetY + (renderHeight * videoRect.bottom - ContextUtils.dip2px(TuSdkContext.context(),8) - layoutParams.height);
                        break;
                    case TopBottom:
                        x =  (parentWidth * videoRect.left + ContextUtils.dip2px(TuSdkContext.context(),8));
                        y = offsetY + (renderHeight * videoRect.bottom - ContextUtils.dip2px(TuSdkContext.context(),8) - layoutParams.height);
                        break;
                    case LeftRight:
                        x =  (parentWidth * videoRect.left + ContextUtils.dip2px(TuSdkContext.context(),8));
                        y = offsetY + (renderHeight * videoRect.bottom - ContextUtils.dip2px(TuSdkContext.context(),8) - layoutParams.height);
                        break;
                }
                mVideoSelectView.setX(x);
                mVideoSelectView.setY(y);
            }
        });
    }

    private void openAlbum() {
        Intent intent = new Intent(MovieRecordFullScreenActivity.this, AlbumActivity.class);
        intent.putExtra("maxSize", 1);
        intent.putExtra("onlyImage", false);
        intent.putExtra("onlyVideo", true);
        intent.putExtra("minSize", -1);
        startActivityForResult(intent, ALBUM_REQUEST_CODE);
    }

    private double allAudioDuration = 0.0;
    private double currentAudioTime = 0.0;

    private void stopAudioRecording(){
        if (mAudioRecord == null)
            return;

        try {
            mAudioRecord.stopRecord();
        } catch (Exception e){

        }
    }

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

    private TuSurfaceTextureHolder mCameraHolder = new TuSurfaceTextureHolder() {

        @Override
        public SurfaceTexture requestSurfaceTexture() {
            mSurfaceTexture = mPipeMediator.getSurfaceTexture();
            return mSurfaceTexture;
        }

        @Override
        public void setInputRotation(ImageOrientation previewOrientation) {
            mPreviewOrientation = previewOrientation;
            mPipeMediator.setInputRotation(previewOrientation.getDegree(),previewOrientation.isMirrored());
        }

        @Override
        public void setInputSize(TuSdkSize previewOptimizeSize) {
            mInputSize = previewOptimizeSize;
            mRecordView.setDisplaySize(mInputSize.width,mInputSize.height);
            mPipeMediator.setInputSize(mInputSize.width,mInputSize.height);

        }

        private long lastTime = 0;
        private long cameraLastTime = 0;

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (!mPipeMediator.isReady()){
                TuSdkSize size = ViewSize.create(mCameraView);
                mCurrentRatio = TuCameraAspectRatio.of(size.width,size.height);
                mPipeMediator.requestInit(getBaseContext(),mCameraView,new SizeF(mCurrentRatio.getX(),mCurrentRatio.getY()));
                mRecordView.initFilterPipe(mPipeMediator.getBeautyManager());
                mCurrentRenderSize = TuSdkSize.create((int) PROCESS_WIDTH, (int) (PROCESS_WIDTH / mCurrentRatio.getX() * mCurrentRatio.getY()));
            }
            Image image = mPipeMediator.onFrameAvailable();
            if (!isRecording){
                image.release();
            }
        }
    };

    private long frameCount = 0l;

    private long processSum = 0L;

    private long cnt = 0;

    private boolean isRecordCompleted = false;

    private boolean isNeedCreateTexture = true;

    private Image mCurrentRes;

    private boolean initText = false;


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
                ThreadHelper.post(new Runnable() {
                    @Override
                    public void run() {
                        init();

                        startCameraCapture();
                    }
                });

            }
            else
            {
                String msg = TuSdkContext.getString("lsq_camera_no_access", ContextUtils.getAppName(MovieRecordFullScreenActivity.this));

                TuSdkViewHelper.alert(permissionAlertDelegate, MovieRecordFullScreenActivity.this, TuSdkContext.getString("lsq_camera_alert_title"),
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
                    Uri.fromParts("package", MovieRecordFullScreenActivity.this.getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @Override
        public void onAlertCancel(AlertDialog dialog)
        {

        }
    };

    private SensorHelper mSensorHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record_full_screen);
        Engine engine = Engine.getInstance();
        engine.init(null);
        if (PermissionUtils.hasRequiredPermissions(this, getRequiredPermissions())){
            mSensorHelper = new SensorHelper(this);
            init();



        } else {
            PermissionUtils.requestRequiredPermissions(this, getRequiredPermissions());
        }
    }

    private void init() {
        initPipe();
        initCamera();
        initRecordView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.handleRequestPermissionsResult(requestCode, permissions, grantResults, this, mGrantedResultDelgate);
    }

    private void initRecordView() {
        mRecordView = findViewById(R.id.lsq_movie_record_view);
        mRecordView.init(getSupportFragmentManager(),getLifecycle());
        mRecordView.initFilterGroupsViews(getSupportFragmentManager(),getLifecycle(), Constants.getCameraFilters(true));
        mRecordView.setUpCamera(this,mCamera);
        mRecordView.setDelegate(new RecordView.TuSDKMovieRecordDelegate() {

            @Override
            public void changedRect(RectF rectF) {
                TuSdkSize wrapSize = mRecordView.getWrapSize();
                mRectSize = TuSdkSize.create(((int) (wrapSize.width * rectF.width())), ((int) (wrapSize.height * rectF.height())));
                mPipeMediator.changedRect(rectF);
                mPipeMediator.updateAspect(new SizeF(mRectSize.width,mRectSize.height));

            }

            private boolean isTimeOut = false;

            @Override
            public boolean startRecording() {
                if (isTimeOut){
                    mRecordView.updateMovieRecordState(RecordView.RecordState.RecordTimeOut,false);
                }
                if (isRecordCompleted) return false;

                String outputPath = getOutputPath();
                Bitmap watermark = BitmapHelper.getRawBitmap(TuSdkContext.context(),R.raw.sample_watermark);
                mAudioRecord = new AudioRecord(mPipeMediator);
                mAudioRecord.updateMicState(true);
                mPipeMediator.startRecord(outputPath, mCurrentRenderSize.width, mCurrentRenderSize.height, watermark, 1, new RecordManager.RecordListener() {
                    @Override
                    public void onProgress(float progress, long ts) {
                        TLog.e("[Debug] record progress %s %s",progress,ts);
                        mRecordView.updateViewOnMovieRecordProgressChanged(progress,ts);
                    }

                    @Override
                    public void onRecordTimeOut() {
                        isRecordCompleted = true;
                        isTimeOut = true;
                        mRecordView.updateMovieRecordState(RecordView.RecordState.RecordCompleted,true);
                    }

                    @Override
                    public void onRecordStart() {
                        isRecording = true;
                        if (mPipeMediator.hasJoiner()){
                            mPipeMediator.setJoinerPlay(true);
                        } else if (mPipeMediator.hasBGM()) {
                            mPipeMediator.setBGMPlay(true);
                        }
                    }

                    @Override
                    public void onRecordPause() {
                        isRecording = false;
                        if (isTimeOut) return;
                        mRecordView.updateMovieRecordState(RecordView.RecordState.Paused,false);
                        if (mPipeMediator.hasJoiner()){
                            mPipeMediator.setJoinerPlay(false);
                        } else if (mPipeMediator.hasBGM()){
                            mPipeMediator.setBGMPlay(false);
                        }
                    }

                    @Override
                    public void onRecordStop() {
                        isTimeOut = false;
                        isRecordCompleted = false;
                    }
                });
                mAudioRecord.startRecord();

                return true;
            }

            @Override
            public boolean isRecording() {
                return isRecording;
            }

            @Override
            public void pauseRecording() {
                stopAudioRecording();
                mPipeMediator.pauseRecord();
            }

            @Override
            public boolean stopRecording() {
                if (mPipeMediator.getCurrentRecordDuration() < Constants.MIN_RECORDING_TIME * 1000 * mCurrentStretch){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg = getString(R.string.lsq_record_time_invalid);
                            Toast.makeText(MovieRecordFullScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }

                mPipeMediator.stopRecord();

                if (mPipeMediator.hasJoiner()){
                    mPipeMediator.joinerSeek(0L);
                    mPipeMediator.setJoinerPlay(false);
                }

                return true;
            }

            @Override
            public void finishRecordActivity() {
                finish();
            }

            @Override
            public void changedAudioEffect(AudioConvert.AudioPitchType type) {
                mPipeMediator.setSoundPitchType(type);
            }

            @Override
            public void changedSpeed(double speed) {
                mPipeMediator.setRecordSpeed(speed);
            }

            @Override
            public void changedRatio(TuSdkSize ratio) {
                TuCameraAspectRatio currentRatio = TuCameraAspectRatio.of(ratio.width,ratio.height);
                mPipeMediator.updateAspect(new SizeF(currentRatio.getX(),currentRatio.getY()));
            }

            @Override
            public int getFragmentSize() {
                return mPipeMediator.getCurrentRecordFragmentSize();
            }

            @Override
            public void popFragment() {
                mPipeMediator.popLastFragment();

                if (mPipeMediator.hasJoiner()){
                    mPipeMediator.joinerSeek(mPipeMediator.getCurrentRecordDuration());
                }
            }

            @Override
            public void selectVideo() {
                openAlbum();

            }

            @Override
            public void updateDoubleViewMode(RecordView.DoubleViewMode mode) {
                if (mode == RecordView.DoubleViewMode.None){
                    mPipeMediator.deleteJoiner();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mVideoSelectView != null)
                                mVideoSelectView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    if (!TextUtils.isEmpty(mCurrentDoubleViewVideoPath)){
                        if (mCurrentMode == mode) return;
                        mCurrentMode = mode;
                        updateDoubleView(mCurrentDoubleViewVideoPath,mCurrentAudioPath);
                    }

                }



            }

            @Override
            public void selectAudio() {
                Intent intent = new Intent(MovieRecordFullScreenActivity.this, AudioListActivity.class);
                startActivityForResult(intent,AUDIO_REQUEST_CODE);
            }

            @Override
            public void updateMicState(boolean isOpen) {
                if (mAudioRecord != null) mAudioRecord.updateMicState(isOpen);
            }

            @Override
            public void changeRenderWidth(double width) {
                mPipeMediator.setRenderWidth((int) width);
            }
        });


    }

    private void initPipe() {
        mPipeMediator = PipeMediator.getInstance();
    }

    private void initCamera() {
        mCameraView = findViewById(R.id.lsq_cameraView);

        mCamera = new TuCameraImpl();

        mCamera.setSurfaceHolder(mCameraHolder);

        mCameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mCurrentRatio == null){
                    TuSdkSize size = ViewSize.create(mCameraView);
                    mCurrentRatio = TuCameraAspectRatio.of(size.width,size.height);
                    mRecordView.setWrapSize(TuSdkSize.create((int) PROCESS_WIDTH,(int) ((double)mCurrentRatio.getY() * PROCESS_WIDTH / (double) mCurrentRatio.getX())));
                }
            }
        });

        mCamera.setFullFrame(false);

        if (!mCamera.prepare()) return;




        mCamera.cameraSize().setPreviewSize(PREVIEW_SIZE);
//        mCamera.cameraSize().setPreviewSize(size);


//        mCamera.cameraSize().setAspectRatio(mCurrentRatio);


        mCamera.cameraBuilder().setDefaultFacing(CameraConfigs.CameraFacing.Front);

        mCamera.cameraOrient().setDisplayRotation(getWindowManager().getDefaultDisplay().getRotation());

        mCamera.cameraOrient().setOutputImageOrientation(InterfaceOrientation.Portrait);
//        mCamera.cameraOrient().setHorizontallyMirrorFrontFacingCamera(false);


//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
//        } else {
//            mCamera.cameraOrient().setHorizontallyMirrorFrontFacingCamera(false);
//        }



        mCamera.setCameraListener(new TuCamera.TuCameraListener() {
            @Override
            public void onStatusChanged(CameraConfigs.CameraState status, TuCamera camera) {
                if (status == START){
                    mRecordView.setExposure();
                }
            }
        });

        mCamera.cameraShot().setShotListener(new TuCameraShot.TuCameraShotListener() {
            @Override
            public void onCameraShotFailed(TuSdkResult data) {

            }

            @Override
            public void onCameraShotBitmap(TuSdkResult data) {
                super.onCameraShotBitmap(data);
                data.image = BitmapHelper.imageCorpResize(data.image,TuSdkSize.create((int) PROCESS_WIDTH,(int) (PROCESS_WIDTH / mCurrentRatio.getX() * mCurrentRatio.getY())),ImageOrientation.Up,false);

                mShotPhoto = mPipeMediator.processBitmap(data.image);

                try {
                    Bitmap photo = mShotPhoto;
                    Bitmap waterMark = BitmapHelper.getRawBitmap(MovieRecordFullScreenActivity.this,R.raw.sample_watermark);

                    int margin = ContextUtils.dip2px(MovieRecordFullScreenActivity.this, 6);

                    int width = photo.getWidth();
                    int height = photo.getHeight();

                    int paddingLeft = width - waterMark.getWidth() - margin;
                    int paddingTop = margin;

                    Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(newb);

                    canvas.drawBitmap(photo, 0, 0, null);

                    canvas.drawBitmap(waterMark, paddingLeft, paddingTop, null);

                    canvas.save();

                    canvas.restore();

                    data.image = newb;

                    mRecordView.presentPreviewLayout(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraCapture();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCameraCapture();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioRecord != null) mAudioRecord.release();
        if (mSensorHelper != null) mSensorHelper.release();

        Engine.getInstance().release();

    }

    private void startCameraCapture(){
        if (mCamera == null) return;
        mCamera.startPreview();

    }

    private void stopCameraCapture(){
        if (mCamera == null) return;
        mCamera.stopPreview();
    }

    private String getOutputPath(){
        String outputFilePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath() + "/camera_output"+System.currentTimeMillis()+".mp4";
        return outputFilePath;
    }

    private String getTempOutputPath(){
        String tempPath = TuSdkContext.getAppCacheDir("recordCache",false).getAbsolutePath() + "/camera_temp"+System.currentTimeMillis()+".mp4";
        return tempPath;
    }
}

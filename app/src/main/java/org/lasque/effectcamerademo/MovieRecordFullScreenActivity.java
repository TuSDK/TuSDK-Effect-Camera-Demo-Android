package org.lasque.effectcamerademo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.tusdk.pulse.Engine;
import com.tusdk.pulse.filter.FileExporter;
import com.tusdk.pulse.filter.Filter;
import com.tusdk.pulse.filter.FilterDisplayView;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.utils.gl.GLContext;
import com.tusdk.pulse.utils.gl.OutputSurface;

import org.lasque.effectcamerademo.utils.Constants;
import org.lasque.effectcamerademo.utils.PermissionUtils;
import org.lasque.effectcamerademo.views.record.RecordView;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.TuSdkResult;
import org.lasque.tusdkpulse.core.listener.TuSdkOrientationEventListener;
import org.lasque.tusdkpulse.core.seles.SelesParameters;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.ContextUtils;
import org.lasque.tusdkpulse.core.utils.FileHelper;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdkpulse.core.utils.image.AlbumHelper;
import org.lasque.tusdkpulse.core.utils.image.BitmapHelper;
import org.lasque.tusdkpulse.core.utils.image.ImageOrientation;
import org.lasque.tusdkpulse.core.utils.sqllite.ImageSqlHelper;
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCamera;
import org.lasque.tusdkpulse.cx.hardware.camera.TuCameraShot;
import org.lasque.tusdkpulse.cx.hardware.camera.impl.TuCameraImpl;
import org.lasque.tusdkpulse.cx.hardware.utils.TuCameraAspectRatio;
import org.lasque.tusdkpulse.cx.seles.extend.TuSurfaceTextureHolder;
import org.lasque.tusdkpulse.modules.view.widget.sticker.StickerData;
import org.lasque.tusdkpulse.modules.view.widget.sticker.StickerFactory;
import org.lasque.tusdkpulse.modules.view.widget.sticker.StickerResult;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.os.Environment.DIRECTORY_DCIM;
import static com.tusdk.pulse.filter.FileExporter.PITCH_TYPE_Normal;
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

    private static long CURRENT_MAX_RECORD_DURATION = 15000;

    private static long MAX_RECORD_DURATION = 15000;

    private FilterDisplayView mCameraView;

    private RecordView mRecordView;

    private TuCamera mCamera;

    private ExecutorService mRenderPool = Executors.newWorkStealingPool(1);

    private ExecutorService mRecordPool = Executors.newFixedThreadPool(1);

    private OutputSurface mOutputSurface;

    private GLContext mGLCtx;

    protected TuSdkSize mInputSize;

    private ImageOrientation mPreviewOrientation;

    private FilterPipe mFP;

    /** SurfaceTexture */
    private SurfaceTexture mSurfaceTexture;

    /** Texture ID */
    private int mTexture = -1;

    private boolean isInitSuccess = false;

    private FileExporter mFileRecorder;

    private boolean isRecording = false;

    private TuSdkOrientationEventListener mOrientationListener;

    private double mCurrentStretch = 1.0;


    // ----------------------- 音频录制 ------------------------- //
    private AudioRecord mAudioRecord;

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

    private RectF mCurrentPreviewRectF = new RectF(0,0,1,1);

    private TuSdkSize mRectSize;

    private boolean initRecorder(){
        int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        int audioBitWidth = AudioFormat.ENCODING_PCM_16BIT;
        int channelCount = 2;
        int bitWidth = 16;

        mBlockByteLength = 1024 * (channelCount) * (bitWidth / 8);

        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioBitWidth);
        int inputBufferSize = minBufferSize * 4;
        int frameSizeInBytes = channelCount * 2;
        mBufferSize = (inputBufferSize / frameSizeInBytes) * frameSizeInBytes;

        if (mBufferSize < 1) {
            return false;
        }

        mAudioRecord = new AudioRecord(audioSource,sampleRate , channelConfig, audioBitWidth, mBufferSize);

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED){
            return false;
        }

        return true;
    }

    private int read(AudioRecord record,byte[] bys){
        int result = 0;
        try {
            result = record.read(bys,0,mBlockByteLength);
        } catch (Exception e){

        }

        if (result < 0){

        }

        return result;
    }

    private double allAudioDuration = 0.0;
    private double currentAudioTime = 0.0;

    private void queueRecording(){
        final AudioRecord record = mAudioRecord;
        final byte[] buffer = new byte[mBufferSize];

        ThreadHelper.runThread(new Runnable() {
            @Override
            public void run() {
                if (record == null || buffer == null || mFileRecorder == null) return;
                while (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                    int total = read(record,buffer);
                    if (isRecording){
                        currentAudioTime = System.currentTimeMillis() - mRecordingStart;
                        mFileRecorder.sendAudioData(buffer,total, (long) currentAudioTime);
                        currentAudioTime *= mCurrentStretch;
//                        TLog.e("Current Audio Time %s length %s buffer size %s",currentAudioTime,total,buffer.length);
                    }
                }

                allAudioDuration += currentAudioTime;
                TLog.e("Current Audio Time %s all %s",currentAudioTime,allAudioDuration);
            }
        });
    }

    private boolean startAudioRecording(){
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            return false;

        try {
            mAudioRecord.startRecording();
            this.queueRecording();
        } catch (Exception e){

        }
        return true;
    }

    private void stopAudioRecording(){
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            return;

        try {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
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
            mSurfaceTexture = mOutputSurface.getSurfaceTexture();
            return mSurfaceTexture;
        }

        @Override
        public void setInputRotation(ImageOrientation previewOrientation) {
            mPreviewOrientation = previewOrientation;
        }

        @Override
        public void setInputSize(TuSdkSize previewOptimizeSize) {
            mInputSize = previewOptimizeSize;
            mRecordView.setDisplaySize(mInputSize.width,mInputSize.height);

            TLog.e("preview size %s",mInputSize.toString());
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            onDrawFrame();
        }
    };

    private long frameCount = 0l;

    private long processSum = 0L;

    private long cnt = 0;

    private boolean isRecordCompleted = false;

    private boolean isNeedCreateTexture = false;

    private void onDrawFrame() {

        long startTime = System.currentTimeMillis();

        //采集到的视频源大小, 未旋转
        TuSdkSize inputSize = mCamera.cameraSize().previewOptimizeSize();


        final TuSdkSize procSize = mRectSize == null ? TuSdkSize.create(720, (int) (720.0 / mCurrentRatio.getX() * mCurrentRatio.getY())) : mRectSize;

        StringBuilder log = new StringBuilder();
        log.append("渲染信息: \n");

        Future<Image> res = mRenderPool.submit(new Callable<Image>() {
            @Override
            public Image call() throws Exception {
                mSurfaceTexture.updateTexImage();
                TuSdkSize size = inputSize.transforOrientation(mPreviewOrientation);

//                TuCameraAspectRatio previewRatio = TuCameraAspectRatio.of(size.width,size.height);
//                TuSdkSize finalProcSize = procSize;
//                if (!previewRatio.equals(mCurrentRatio)){
//                    finalProcSize = TuSdkSize.create(size.width ,size.width / mCurrentRatio.getX() * mCurrentRatio.getY());
//                }
                if (mTexture <0 || isNeedCreateTexture){
                    int deleteTextureID = mTexture;

                    int[] textures = new int[1];
                    GLES20.glGenTextures(1, textures, 0);
                    int textureID = textures[0];

                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR);
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                            GLES20.GL_LINEAR);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                            GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                            GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                            0,
                            GLES20.GL_RGBA,
                            procSize.width, procSize.height,
                            0,
                            GLES20.GL_RGBA,
                            GLES20.GL_UNSIGNED_BYTE,
                            null);

                    mTexture = textureID;

                    if (deleteTextureID != -1){
                        int[] deleteTextures = new int[1];
                        textures[0] = deleteTextureID;
                        GLES20.glDeleteTextures(1,deleteTextures,0);
                    }
//
//                    GLES20.glFinish();

                    isNeedCreateTexture = false;
                }


                //Log.e("DEBUG ", "call: " + cnt++ );
                int drawRes = mOutputSurface.drawImageTo(mTexture, size.width,size.height, procSize.width, procSize.height);
//                GLES20.glFinish();
                long surfaceDrawDuration = System.currentTimeMillis() - startTime;
                log.append("采集分辨率 : ").append(inputSize).append("\n");
                log.append("渲染分辨率 : ").append(procSize).append(" \n");
                log.append("OutputSurface 处理时长 : ").append(surfaceDrawDuration).append(" \n");
                if (drawRes < 0) return null;
                Image in = new Image(mTexture, procSize.width, procSize.height,System.currentTimeMillis());
//                Image in = new Image(testBitmap,System.currentTimeMillis());
//                Bitmap bitmap = in.toBitmap();
                double agree = mOrientationListener.getDeviceAngle() + InterfaceOrientation.Portrait.getDegree();

                boolean enableMarkSence = false;
                if (mFP.getFilter(RecordView.mFilterMap.get(SelesParameters.FilterModel.Reshape)) != null
                        || mFP.getFilter(RecordView.mFilterMap.get(SelesParameters.FilterModel.CosmeticFace)) != null){


                    enableMarkSence = mRecordView.checkEnableMarkSence();
                }

                TLog.e("enable check sence %s",enableMarkSence);
                in.setMarkSenceEnable(enableMarkSence);
                in.setAgree(agree);
                frameCount ++;
                Image out = mFP.process(in);
                in.release();
                long fpProcessDuration = System.currentTimeMillis() - startTime;
                log.append("FilterPipe 处理时长 : ").append(fpProcessDuration).append(" \n");
                log.append("degree : ").append(agree).append(" \n");
//                TLog.e("output image %s",out);
                return out;
            }
        });
        try {
            Image out = res.get();
            if (mCurrentPreviewRectF == null)
                mCameraView.updateImage(out);
            else mCameraView.updateImage(out,mCurrentPreviewRectF);
            long processDuration = System.currentTimeMillis() - startTime;
            log.append("当前帧渲染总时长 : ").append(processDuration).append("\n");

            if (mFileRecorder != null && isRecording){
                mRecordPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mFileRecorder != null && isRecording){
                            mCurrentFragmentDuration = System.currentTimeMillis() - mRecordingStart;
                            mFileRecorder.sendImage(out,mCurrentFragmentDuration);
                            mCurrentFragmentDuration *= mCurrentStretch;
                            TLog.e("Duration fragment : %s current : %s",mCurrentFragmentDuration,mCurrentDuration);
                            float progress = (mCurrentFragmentDuration + mCurrentDuration) / (float) CURRENT_MAX_RECORD_DURATION;
                            if (progress > 1){
                                if (!isRecordCompleted){
                                    isRecordCompleted = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRecordView.updateMovieRecordState(RecordView.RecordState.RecordCompleted,true);

                                        }
                                    });
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordView.updateViewOnMovieRecordProgressChanged(progress,mCurrentFragmentDuration);
                                    }
                                });

                            }
                            TLog.e("end send image");
                        }

                        out.release();
                    }
                });
            } else {
                out.release();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.log)).setText(log.toString());
                }
            });
            processSum += processDuration;
            if (frameCount % 150 == 0){
                double avg = processSum / (double)frameCount;
                TLog.e("[Debug] Current Frame process Duration %s process duration average value %s frame count %s",processDuration,avg,frameCount);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                ThreadHelper.post(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        mOrientationListener = new TuSdkOrientationEventListener(MovieRecordFullScreenActivity.this);
                        mOrientationListener.enable();

                        startCameraCapture();
                        while (!mCameraView.isAvailable()){
                            TLog.e("wait camera view");
                        }
                        mCameraView.onSurfaceTextureAvailable(mCameraView.getSurfaceTexture(),mCameraView.getWidth(),mCameraView.getHeight());
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

    private Bitmap testBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record_full_screen);
        Engine engine = Engine.getInstance();
        engine.init(null);
        if (PermissionUtils.hasRequiredPermissions(this, getRequiredPermissions())){
            init();

            mOrientationListener = new TuSdkOrientationEventListener(this);
            mOrientationListener.enable();
        } else {
            PermissionUtils.requestRequiredPermissions(this, getRequiredPermissions());
        }

        testBitmap = BitmapHelper.getRawBitmap(MovieRecordFullScreenActivity.this,R.raw.lsq_filter_thumb_foodcaramel1);
    }

    private void init() {
        initFilterPipe();
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
        mRecordView.initFilterPipe(mFP,mRenderPool);
        mRecordView.initFilterGroupsViews(getSupportFragmentManager(),getLifecycle(), Constants.getCameraFilters(true));
        mRecordView.setUpCamera(this,mCamera);
        mRecordView.setCameraView(mCameraView);
        mRecordView.setDelegate(new RecordView.TuSDKMovieRecordDelegate() {

            @Override
            public void changedRect(RectF rectF) {
                mCurrentPreviewRectF = rectF;

                Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        TuSdkSize wrapSize = mRecordView.getWrapSize();
                        mRectSize = TuSdkSize.create(((int) (wrapSize.width * rectF.width())), ((int) (wrapSize.height * rectF.height())));

//                        TuCameraAspectRatio ratio = TuCameraAspectRatio.of(((int) (rectF.width() * 1000)), ((int) (rectF.height() * 1000)));
//                        mRectRatio = ratio;
                        isNeedCreateTexture = true;
//                        TLog.e("current rect ratio %s rect %s",ratio,rectF);
                        return isNeedCreateTexture;
                    }
                });

                try {
                    res.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public boolean startRecording() {
                float progress = (mCurrentFragmentDuration + mCurrentDuration) / (float) CURRENT_MAX_RECORD_DURATION;
                if (progress>1){
                    mRecordView.updateMovieRecordState(RecordView.RecordState.RecordTimeOut,false);
                    return false;
                }

                mRecordPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mFileRecorder == null ) {
                            mFileRecorder = new FileExporter();
                            String outputFilePath = getTempOutputPath();
                            mCurrentFragment = new VideoFragmentItem();
                            mCurrentFragment.path = outputFilePath;
                            mVideoLists.add(mCurrentFragment);
                            FileExporter.Config config = new FileExporter.Config();
                            TuSdkSize size = mCamera.cameraSize().previewOptimizeSize().transforOrientation(mPreviewOrientation);
                            config.channels = 2;
                            size.width = (int) (size.height / mCurrentRatio.getY() * mCurrentRatio.getX());
                            size = size.evenSize();
                            config.height = size.height;
                            config.width = size.width;
                            config.sampleRate = 44100;
                            config.stretch = mCurrentStretch;
                            config.savePath = outputFilePath;
                            config.pitchType = mCurrentAudioEffect;
                            config.watermark = BitmapHelper.getRawBitmap(MovieRecordFullScreenActivity.this,R.raw.sample_watermark);
                            config.watermarkPosition = 1;
                            mFileRecorder.open(config);
                        }
                        if (mRecordingStart == 0 )mRecordingStart = System.currentTimeMillis();
                        if (mAudioRecord == null) {
                            initRecorder();
                            startAudioRecording();
                        }
                        isRecording = true;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordView.updateMovieRecordState(RecordView.RecordState.Recording,isRecording);
                                isRecordCompleted = false;
                            }
                        });
                    }
                });
                return true;
            }

            @Override
            public boolean isRecording() {
                return isRecording;
            }

            @Override
            public void pauseRecording() {
                mRecordPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        isRecording = false;
                        stopAudioRecording();
                        if (mFileRecorder != null)
                            mFileRecorder.close();
                        mCurrentFragment.fragmentDuration = mCurrentFragmentDuration;
                        mFileRecorder = null;
                        mRecordingStart = 0L;
                        mCurrentDuration += mCurrentFragmentDuration;

                        mCurrentFragmentDuration = 0L;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordView.updateMovieRecordState(RecordView.RecordState.Paused,isRecording);
                            }
                        });
                    }
                });
            }

            @Override
            public void stopRecording() {
                TLog.e("Duration fragment : %s current : %s",mCurrentFragmentDuration,mCurrentDuration);
                TLog.e("Current Audio Time %s all %s",currentAudioTime,allAudioDuration);
                mRecordPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String outputPath = getOutputPath();
                        if (mVideoLists.size() > 1){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecordView.updateMovieRecordState(RecordView.RecordState.Saving,isRecording);
                                }
                            });
                            String[] paths = new String[mVideoLists.size()];
                            for (int i =0;i<mVideoLists.size();i++) paths[i] = mVideoLists.get(i).path;
                            FileExporter.MergeVideoFiles(outputPath, paths);
                        } else {
                            FileHelper.rename(new File(mVideoLists.get(0).path),new File(outputPath));
//                    FileHelper.delete(new File(mVideoLists.get(0).path));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(new File(outputPath));
                                intent.setData(uri);
                                sendBroadcast(intent);
                                mRecordView.updateMovieRecordState(RecordView.RecordState.SaveCompleted,isRecording);

                            }
                        });
                        mVideoLists.clear();
                        mCurrentDuration = 0;
                        allAudioDuration = 0;
                    }
                });


            }

            @Override
            public void finishRecordActivity() {
                finish();
            }

            @Override
            public void changedAudioEffect(int effect) {
                mCurrentAudioEffect = effect;
            }

            @Override
            public void changedSpeed(double speed) {
                mCurrentStretch = speed;
//                CURRENT_MAX_RECORD_DURATION = (long) (MAX_RECORD_DURATION / speed);
            }

            @Override
            public void changedRatio(TuSdkSize ratio) {
                Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        TuCameraAspectRatio currentRatio = TuCameraAspectRatio.of(ratio.width,ratio.height);
                        if (!currentRatio.equals(mCurrentRatio)){
                            mCurrentRatio = currentRatio;
//                            mCamera.cameraSize().setAspectRatio(mCurrentRatio);
//                            mCamera.stopPreview();
//                            mCamera.startPreview();

                            isNeedCreateTexture = true;
                        }
                        return true;
                    }
                });

                try {
                    res.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public int getFragmentSize() {
                return mVideoLists.size();
            }

            @Override
            public void popFragment() {
                if (mVideoLists.size() == 0) return;
                VideoFragmentItem item = mVideoLists.get(mVideoLists.size() - 1);
                mCurrentDuration -= item.fragmentDuration;
                mVideoLists.remove(item);
                isRecordCompleted = false;
            }
        });


    }

    private void initFilterPipe() {
        Future<Boolean> res = mRenderPool.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mGLCtx = new GLContext();

                mGLCtx.createForRender(Engine.getInstance().getMainGLContext().getEGLContext());

                mGLCtx.makeCurrent();

                mOutputSurface = new OutputSurface();

                mOutputSurface.create(mGLCtx);

                mFP = new FilterPipe();

                isInitSuccess = mFP.create();

                return isInitSuccess;
            }
        });

        Future<Boolean> recordRes = mRecordPool.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mGLCtx.makeCurrent();
                return true;
            }
        });


        try {
            res.get();
            recordRes.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initCamera() {
        mCameraView = findViewById(R.id.lsq_cameraView);

        mCamera = new TuCameraImpl();

        mCamera.setSurfaceHolder(mCameraHolder);

        mCameraView.init(Engine.getInstance().getMainGLContext());

        mCameraView.setBackgroundColor(Color.BLACK);

        mCameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mCurrentRatio == null){
                    TuSdkSize size = ViewSize.create(mCameraView);
                    mCurrentRatio = TuCameraAspectRatio.of(size.width,size.height);
                    mRecordView.setWrapSize(TuSdkSize.create(720,720 / mCurrentRatio.getX() * mCurrentRatio.getY()));
                }
            }
        });

        mCamera.setFullFrame(false);

//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//
//                byte[] cloneDate = data.clone();
//
//                // todo
//
//                camera.addCallbackBuffer(data);
//            }
//        });


        if (!mCamera.prepare()) return;




        mCamera.cameraSize().setPreviewSize(TuSdkSize.create(1280,720));
//        mCamera.cameraSize().setPreviewSize(size);


//        mCamera.cameraSize().setAspectRatio(mCurrentRatio);

        mCamera.cameraBuilder().setDefaultFacing(CameraConfigs.CameraFacing.Front);

        mCamera.cameraOrient().setOutputImageOrientation(InterfaceOrientation.Portrait);

        mCamera.cameraOrient().setHorizontallyMirrorFrontFacingCamera(true);

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
                data.image = BitmapHelper.imageCorpResize(data.image,TuSdkSize.create(720,720 / mCurrentRatio.getX() * mCurrentRatio.getY()),ImageOrientation.Up,false);

                Future<Bitmap> res = mRenderPool.submit(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Image input = new Image(data.image,System.currentTimeMillis());
                        Image output = mFP.process(input);
                        return output.toBitmap();
                    }
                });

                try {
                    Bitmap photo = res.get();
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
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
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

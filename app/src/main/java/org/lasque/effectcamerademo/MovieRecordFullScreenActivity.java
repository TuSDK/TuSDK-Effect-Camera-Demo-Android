package org.lasque.effectcamerademo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
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
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.tusdk.pulse.Config;
import com.tusdk.pulse.DispatchQueue;
import com.tusdk.pulse.Engine;
import com.tusdk.pulse.MediaInspector;
import com.tusdk.pulse.filter.FileExporter;
import com.tusdk.pulse.filter.FileRecordAudioMixer;
import com.tusdk.pulse.filter.Filter;
import com.tusdk.pulse.filter.FilterDisplayView;
import com.tusdk.pulse.filter.FilterPipe;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.filter.filters.SimultaneouslyFilter;
import com.tusdk.pulse.utils.gl.GLContext;
import com.tusdk.pulse.utils.gl.OutputSurface;

import org.lasque.effectcamerademo.album.AlbumActivity;
import org.lasque.effectcamerademo.album.AlbumInfo;
import org.lasque.effectcamerademo.audio.AudioItem;
import org.lasque.effectcamerademo.audio.AudioListActivity;
import org.lasque.effectcamerademo.audio.AudioMixerItem;
import org.lasque.effectcamerademo.audio.AudioRecord;
import org.lasque.effectcamerademo.utils.Constants;
import org.lasque.effectcamerademo.utils.PermissionUtils;
import org.lasque.effectcamerademo.views.record.RecordView;
import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.TuSdkResult;
import org.lasque.tusdkpulse.core.listener.TuSdkOrientationEventListener;
import org.lasque.tusdkpulse.core.seles.SelesParameters;
import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.struct.TuSdkSizeF;
import org.lasque.tusdkpulse.core.struct.ViewSize;
import org.lasque.tusdkpulse.core.utils.ContextUtils;
import org.lasque.tusdkpulse.core.utils.FileHelper;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.TuSdkDate;
import org.lasque.tusdkpulse.core.utils.hardware.CameraConfigs;
import org.lasque.tusdkpulse.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdkpulse.core.utils.image.AlbumHelper;
import org.lasque.tusdkpulse.core.utils.image.BitmapHelper;
import org.lasque.tusdkpulse.core.utils.image.ImageOrientation;
import org.lasque.tusdkpulse.core.utils.image.RatioType;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import static com.tusdk.pulse.filter.filters.SimultaneouslyFilter.PROP_RECT_PARAM;
import static com.tusdk.pulse.filter.filters.SimultaneouslyFilter.PROP_SEEK_PARAM;
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

    private static long CURRENT_MAX_RECORD_DURATION = 15000;

    private static long MAX_RECORD_DURATION = 15000;

    private static int AUDIO_REQUEST_CODE = 4;

    private FilterDisplayView mCameraView;

    private RecordView mRecordView;

    private TuCamera mCamera;

//    private ExecutorService mRenderPool = Executors.newWorkStealingPool(1);Filter
    private DispatchQueue mRenderPool = new DispatchQueue();

    private DispatchQueue mRecordPool = new DispatchQueue();

    private OutputSurface mOutputSurface;

    private GLContext mGLCtx;

    protected TuSdkSize mInputSize;

    protected TuSdkSize mCurrentRenderSize;

    private ImageOrientation mPreviewOrientation;

    private FilterPipe mFP;

    /** SurfaceTexture */
    private SurfaceTexture mSurfaceTexture;

    /** Texture ID */
    private int mTexture = -1;
    private final int mTexCount = 4;
    private int[] mTextures = new int[mTexCount];
    private int mTexIdx = 0;

    private boolean isInitSuccess = false;

    private FileExporter mFileRecorder;

    private boolean isRecording = false;

    private TuSdkOrientationEventListener mOrientationListener;

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

    private RectF mCurrentPreviewRectF = new RectF(0,0,1,1);

    private TuSdkSize mRectSize;

    private SimultaneouslyFilter.PropertyBuilder siBuilder = new SimultaneouslyFilter.PropertyBuilder();

    private String mCurrentDoubleViewVideoPath = "";

    private String mCurrentAudioPath = "";

    private RecordView.DoubleViewMode mCurrentMode;

    private AudioMixerItem mAudioItem;

    private FileRecordAudioMixer mAudioMixer;

    private AudioRecord mAudioRecord;

    private long mCurrentPlayerPos = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALBUM_REQUEST_CODE){
            if (resultCode == ALBUM_RESULT_CODE){


                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        TuSdkSize size = ViewSize.create(mCameraView);
                        mCurrentRatio = TuCameraAspectRatio.of(size.width,size.height);
                        mRectSize = TuSdkSize.create(720, (int) (720.0 / mCurrentRatio.getX() * mCurrentRatio.getY()));
                        mCurrentRenderSize = mRectSize;
                        isNeedCreateTexture = true;
                    }
                });
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
                updateDoubleView(info.getPath());

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

    private void initAudioMixer(String audioPath) {
        MediaInspector inspector = MediaInspector.shared();
        MediaInspector.MediaInfo info = inspector.inspect(audioPath);
        for (MediaInspector.MediaInfo.AVItem item : info.streams){
            if (item instanceof MediaInspector.MediaInfo.Audio){
                AudioMixerItem audioItem = new AudioMixerItem(audioPath,item.bitrate,((MediaInspector.MediaInfo.Audio) item).channels,((MediaInspector.MediaInfo.Audio) item).sampleRate);
                mAudioItem = audioItem;
                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        if (mAudioMixer != null){
                            mAudioMixer.close();
                            mAudioMixer = null;
                        }

                        mAudioMixer = new FileRecordAudioMixer();
                        Config config = new Config();
                        config.setString(FileRecordAudioMixer.CONFIG_PATH,mAudioItem.getPath());
//                        config.setNumber(FileRecordAudioMixer.CONFIG_SAMPLE_COUNT,mAudioItem.getSampleCount());
                        config.setNumber(FileRecordAudioMixer.CONFIG_CHANNELS,mAudioItem.getChannels());
                        config.setNumber(FileRecordAudioMixer.CONFIG_SAMPLE_RATE,mAudioItem.getSampleRate());
                        mAudioMixer.open(config);

                        TLog.e("current audio mixer item %s",mAudioItem);
                        ThreadHelper.runThread(new Runnable() {
                            @Override
                            public void run() {
                                int channels = mAudioItem.getChannels() == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;

                                int bufferSize = AudioTrack.getMinBufferSize(mAudioItem.getSampleRate(),channels,AudioFormat.ENCODING_PCM_16BIT);

                                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mAudioItem.getSampleRate(), channels, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                                audioTrack.setVolume(1f);
                                audioTrack.play();
                                int length = 0;

                                byte[] buffer = new byte[bufferSize];

                                while (true){
                                    int res = mAudioMixer.getPCMForPlay(buffer,bufferSize);
                                    if (res == -2){
                                        break;
                                    } else if (res < 0){
                                        continue;
                                    }
                                    TLog.e("getPCMForPlay -- 1 bufferSize %s res %s ",bufferSize,res);
                                    length = audioTrack.write(buffer,0,bufferSize);
                                    TLog.e("getPCMForPlay -- 2 result %s",length);
                                }

                                audioTrack.stop();
                                audioTrack.release();
                                audioTrack = null;
                            }
                        });
                    }
                });


                break;
            }
        }
    }

    /**
     * @param path
     */
    private void updateDoubleView(String path) {

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
        mRenderPool.runAsync(new Runnable() {
            @Override
            public void run() {
                mFP.deleteFilter(RecordView.DOUBLE_VIEW_INDEX);

                Filter filter = new Filter(mFP.getContext(), SimultaneouslyFilter.TYPE_NAME);
                Config config = new Config();
                config.setNumber(SimultaneouslyFilter.CONFIG_WIDTH,mCurrentRenderSize.width);
                config.setNumber(SimultaneouslyFilter.CONFIG_HEIGHT,mCurrentRenderSize.height);

                config.setString(SimultaneouslyFilter.CONFIG_PATH,path);
                config.setString(SimultaneouslyFilter.CONFIG_FIRST_LAYER,SimultaneouslyFilter.INDEX_CAMERA);
                config.setNumber(SimultaneouslyFilter.CONFIG_STRETCH,mCurrentVideoStretch);
                config.setNumber(SimultaneouslyFilter.CONFIG_FRAMERATE,30);

                filter.setConfig(config);

                mFP.addFilter(RecordView.DOUBLE_VIEW_INDEX,filter);

                SimultaneouslyFilter.PropertyHolder holder = new SimultaneouslyFilter.PropertyHolder();
                holder.video_dst_rect = videoRect;
                holder.camera_dst_rect = cameraRect;
                SimultaneouslyFilter.PropertyBuilder builder = new SimultaneouslyFilter.PropertyBuilder();
                builder.holder = holder;
                filter.setProperty(PROP_RECT_PARAM,builder.makeRectProperty());

                holder.current_pos = (int) mCurrentPlayerPos;

                filter.setProperty(PROP_SEEK_PARAM,builder.makeSeekProperty());

            }
        });

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

        private long lastTime = 0;
        private long cameraLastTime = 0;

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mRenderPool.runAsync(new Runnable() {
                @Override
                public void run() {
                    onDrawFrame();
                    long time = System.currentTimeMillis();
                    long diff = time - lastTime;
                    lastTime = time;
                    TLog.e("draw diff : %s fps : %s",diff,1000.0/diff);
                }
            });
//
//
            long time = System.currentTimeMillis();
            long diff = time - cameraLastTime;
            cameraLastTime = time;
            TLog.e("camera diff : %s fps : %s",diff,1000.0/diff);
//            mRenderPool.runAsync(new Runnable() {
//                @Override
//                public void run() {
//                    mSurfaceTexture.updateTexImage();
//
//                }
//            });

        }
    };

    private long frameCount = 0l;

    private long processSum = 0L;

    private long cnt = 0;

    private boolean isRecordCompleted = false;

    private boolean isNeedCreateTexture = true;

    private Image mCurrentRes;

    private void onDrawFrame() {
        TLog.fps("on draw frame");

        long startTime = System.currentTimeMillis();

        //采集到的视频源大小, 未旋转
        TuSdkSize inputSize = mCamera.cameraSize().previewOptimizeSize();


        final TuSdkSize procSize = (mRectSize == null ? TuSdkSize.create(720, (int) (720.0 / mCurrentRatio.getX() * mCurrentRatio.getY())) : mRectSize).evenSize();
        mCurrentRenderSize = procSize;
        StringBuilder log = new StringBuilder();
        log.append("渲染信息: \n");

        mSurfaceTexture.updateTexImage();
        TuSdkSize size = inputSize.transforOrientation(mPreviewOrientation);

//                TuCameraAspectRatio previewRatio = TuCameraAspectRatio.of(size.width,size.height);
//                TuSdkSize finalProcSize = procSize;
//                if (!previewRatio.equals(mCurrentRatio)){
//                    finalProcSize = TuSdkSize.create(size.width ,size.width / mCurrentRatio.getX() * mCurrentRatio.getY());
//                }
        //FIXME: 没有释放时机
        if (isNeedCreateTexture) {
            GLES20.glGenTextures(mTexCount, mTextures, 0);

            for (int idx = 0; idx < mTexCount; idx++) {

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[idx]);

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
            }
            isNeedCreateTexture = false;
        }
//        if (mTexture <0 || isNeedCreateTexture){
//            int deleteTextureID = mTexture;
//
//            int[] textures = new int[1];
//            GLES20.glGenTextures(1, textures, 0);
//            int textureID = textures[0];
//
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
//
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
//                    GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
//                    GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
//                    GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
//                    GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
//                    0,
//                    GLES20.GL_RGBA,
//                    procSize.width, procSize.height,
//                    0,
//                    GLES20.GL_RGBA,
//                    GLES20.GL_UNSIGNED_BYTE,
//                    null);
//
//            mTexture = textureID;
//
//            if (deleteTextureID != -1){
//                int[] deleteTextures = new int[1];
//                textures[0] = deleteTextureID;
//                GLES20.glDeleteTextures(1,deleteTextures,0);
//            }
////
////                    GLES20.glFinish();
//
//            isNeedCreateTexture = false;
//        }

        mTexture = mTextures[mTexIdx];
        Log.e("DEBUG ", "zzzx call: " + cnt++ + ", tex: " + mTextures[mTexIdx] + ", idx: " + mTexIdx);

        mTexIdx = (mTexIdx + 1) % mTexCount;

        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);

        int drawRes = mOutputSurface.drawImageTo(mTexture, size.width,size.height, procSize.width, procSize.height);
//                GLES20.glFinish();
        long surfaceDrawDuration = System.currentTimeMillis() - startTime;
        log.append("采集分辨率 : ").append(inputSize).append("\n");
        log.append("渲染分辨率 : ").append(procSize).append(" \n");
        log.append("OutputSurface 处理时长 : ").append(surfaceDrawDuration).append(" \n");
        if (drawRes < 0) return;
        long inputPos = System.currentTimeMillis();
        Image in = new Image(mTexture, procSize.width, procSize.height,inputPos);
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

        mCurrentRes = out;
        try {
            if (mCurrentPreviewRectF == null || mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX) != null)
                mCameraView.updateImage(out);
            else mCameraView.updateImage(out,mCurrentPreviewRectF);
            long processDuration = System.currentTimeMillis() - startTime;
            log.append("当前帧渲染总时长 : ").append(processDuration).append("\n");
//            double fps = 1000.0 / processDuration;
//            log.append("当前渲染帧率 : ").append(fps).append("\n");
            double fps = TLog.fps("fps");
            log.append("当前渲染帧率 : ").append(fps).append("\n");

            if (mFileRecorder != null && isRecording){
                mRecordPool.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        if (mFileRecorder != null && isRecording){
                            mCurrentFragmentDuration = inputPos - mRecordingStart;
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
        } catch (Exception e) {
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

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        TuSdkSize wrapSize = mRecordView.getWrapSize();
                        mRectSize = TuSdkSize.create(((int) (wrapSize.width * rectF.width())), ((int) (wrapSize.height * rectF.height())));

//                        TuCameraAspectRatio ratio = TuCameraAspectRatio.of(((int) (rectF.width() * 1000)), ((int) (rectF.height() * 1000)));
//                        mRectRatio = ratio;
                        isNeedCreateTexture = true;
                    }
                });

            }

            @Override
            public boolean startRecording() {
                float progress = (mCurrentFragmentDuration + mCurrentDuration) / (float) CURRENT_MAX_RECORD_DURATION;
                if (progress>1){
                    mRecordView.updateMovieRecordState(RecordView.RecordState.RecordTimeOut,false);
                    return false;
                }

                mRecordPool.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        if (mFileRecorder == null ) {
                            mFileRecorder = new FileExporter();
                            String outputFilePath = getTempOutputPath();
                            mCurrentFragment = new VideoFragmentItem();
                            mCurrentFragment.path = outputFilePath;
                            mVideoLists.add(mCurrentFragment);
                            FileExporter.Config config = new FileExporter.Config();
                            config.channels = 2;
                            TuSdkSize size = mCurrentRenderSize.evenSize();
                            config.height = size.height;
                            config.width = size.width;
                            config.sampleRate = 44100;
                            config.stretch = mCurrentStretch;
                            config.savePath = outputFilePath;
                            config.pitchType = mCurrentAudioEffect;
                            config.watermark = BitmapHelper.getRawBitmap(MovieRecordFullScreenActivity.this,R.raw.sample_watermark);
                            config.watermarkPosition = 1;
                            boolean res = mFileRecorder.open(config);
                            if (!res){
                                return;
                            }

                        }
                        if (!TextUtils.isEmpty(mCurrentAudioPath)) mAudioRecord.initAudioMixer(mCurrentAudioPath,mCurrentPlayerPos);
                        if (mRecordingStart == 0 )mRecordingStart = System.currentTimeMillis();


                        mAudioRecord.startRecord(mFileRecorder);
                        isRecording = true;

                        mRenderPool.runSync(new Runnable() {
                            @Override
                            public void run() {
                                Filter filter = mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX);
                                if (filter == null) return;
                                siBuilder.holder.enable_play = true;
                                filter.setProperty(SimultaneouslyFilter.PROP_PARAM,siBuilder.makeProperty());
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordView.updateMovieRecordState(RecordView.RecordState.Recording,isRecording);
                                isRecordCompleted = false;
                                if (mVideoSelectView != null)
                                    mVideoSelectView.setVisibility(View.GONE);
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
                mRecordPool.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        isRecording = false;
                        stopAudioRecording();
                        if (mFileRecorder != null)
                            mFileRecorder.close();

                        mAudioRecord.resetAudioMixer();

                        mCurrentFragment.fragmentDuration = mCurrentFragmentDuration;
                        mFileRecorder = null;
                        mRecordingStart = 0L;
                        mCurrentDuration += mCurrentFragmentDuration;

                        mCurrentFragmentDuration = 0L;

                        mCurrentPlayerPos = mCurrentDuration;

                        mRenderPool.runSync(new Runnable() {
                            @Override
                            public void run() {
                                Filter filter = mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX);
                                if (filter == null) return;
                                siBuilder.holder.enable_play = false;
                                filter.setProperty(SimultaneouslyFilter.PROP_PARAM,siBuilder.makeProperty());
                            }
                        });

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
            public boolean stopRecording() {
                TLog.e("Duration fragment : %s current : %s",mCurrentFragmentDuration,mCurrentDuration);
                TLog.e("Current Audio Time %s all %s",currentAudioTime,allAudioDuration);
                if (mCurrentDuration < Constants.MIN_RECORDING_TIME * 1000 * mCurrentStretch){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg = getString(R.string.lsq_record_time_invalid);
                            Toast.makeText(MovieRecordFullScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }

                mRecordPool.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        String outputPath = getOutputPath();
                        String tempPath = getTempOutputPath();
                        if (mVideoLists.size() > 1){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecordView.updateMovieRecordState(RecordView.RecordState.Saving,isRecording);
                                }
                            });
                            String[] paths = new String[mVideoLists.size()];
                            for (int i =0;i<mVideoLists.size();i++) paths[i] = mVideoLists.get(i).path;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                FileExporter.MergeVideoFiles(tempPath, paths);
                                long now = TuSdkDate.create().getTimeInMillis();
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.DATE_TAKEN, now);
                                values.put(MediaStore.Images.Media.DATE_MODIFIED, now / 1000);
                                values.put(MediaStore.Images.Media.DATE_ADDED, now / 1000);
                                values.put(MediaStore.Images.Media.DISPLAY_NAME,outputPath.substring(outputPath.lastIndexOf("/")));
                                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                                Uri uri = TuSdkContext.context().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                                try {
                                    OutputStream outputStream = TuSdkContext.context().getContentResolver().openOutputStream(uri);
                                    File cacheFile = new File(tempPath);
                                    InputStream stream = FileHelper.getFileInputStream(cacheFile);
                                    FileHelper.copy(stream,outputStream);
                                    FileHelper.safeClose(stream);
                                    FileHelper.safeClose(outputStream);
                                    values.clear();
                                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                                    TuSdkContext.context().getContentResolver().update(uri, values, null, null);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                FileExporter.MergeVideoFiles(outputPath, paths);
                            }


                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                long now = TuSdkDate.create().getTimeInMillis();
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.DATE_TAKEN, now);
                                values.put(MediaStore.Images.Media.DATE_MODIFIED, now / 1000);
                                values.put(MediaStore.Images.Media.DATE_ADDED, now / 1000);
                                values.put(MediaStore.Images.Media.DISPLAY_NAME,outputPath.substring(outputPath.lastIndexOf("/")));
                                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                                Uri uri = TuSdkContext.context().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                                try {
                                    OutputStream outputStream = TuSdkContext.context().getContentResolver().openOutputStream(uri);
                                    File cacheFile = new File(mVideoLists.get(0).path);
                                    InputStream stream = FileHelper.getFileInputStream(cacheFile);
                                    FileHelper.copy(stream,outputStream);
                                    FileHelper.safeClose(stream);
                                    FileHelper.safeClose(outputStream);
                                    values.clear();
                                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                                    TuSdkContext.context().getContentResolver().update(uri, values, null, null);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                boolean renameSuccess = FileHelper.rename(new File(mVideoLists.get(0).path),new File(outputPath));
                                TLog.e("file rename %s to %s is success %s",mVideoLists.get(0).path,outputPath,renameSuccess);
                            }



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
                                if (mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX) != null){
                                    mVideoSelectView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        mVideoLists.clear();
                        mCurrentDuration = 0;
                        allAudioDuration = 0;
                        mCurrentPlayerPos = 0;
                    }
                });

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        Filter filter = mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX);
                        if (filter == null) return;
                        siBuilder.holder.current_pos = 0;
                        filter.setProperty(PROP_SEEK_PARAM,siBuilder.makeSeekProperty());
                    }
                });

                return true;
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
                TLog.e("current stretch %s",speed);
                mCurrentStretch = speed;
                if (mCurrentStretch == 2.0){
                    mCurrentVideoStretch = 0.5;
                } else if (mCurrentStretch == 1.5){
                    mCurrentVideoStretch = 0.75;
                } else if (mCurrentStretch == 0.75){
                    mCurrentVideoStretch = 1.5;
                } else if (mCurrentStretch == 0.5){
                    mCurrentVideoStretch = 2.0;
                } else {
                    mCurrentVideoStretch = mCurrentStretch;
                }

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        if (mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX) != null){
                            updateDoubleView(mCurrentDoubleViewVideoPath);
                        }
                    }
                });

                mAudioRecord.updateAudioStretch(mCurrentVideoStretch);
//                CURRENT_MAX_RECORD_DURATION = (long) (MAX_RECORD_DURATION / speed);
            }

            @Override
            public void changedRatio(TuSdkSize ratio) {

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        TuCameraAspectRatio currentRatio = TuCameraAspectRatio.of(ratio.width,ratio.height);
                        if (!currentRatio.equals(mCurrentRatio)){
                            mCurrentRatio = currentRatio;
//                            mCamera.cameraSize().setAspectRatio(mCurrentRatio);
//                            mCamera.stopPreview();
//                            mCamera.startPreview();

                            isNeedCreateTexture = true;
                        }
                    }
                });
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
                mCurrentPlayerPos = mCurrentDuration;

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        Filter filter = mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX);
                        if (filter == null) {
                            if (mCurrentDuration == 0){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordView.updateAudioNameState(View.VISIBLE);
                                    }
                                });
                            }
                            return;
                        }
                        siBuilder.holder.current_pos = (int) mCurrentDuration;
                        filter.setProperty(PROP_SEEK_PARAM,siBuilder.makeSeekProperty());
                        if (mVideoLists.isEmpty()){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mVideoSelectView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void selectVideo() {
                openAlbum();

            }

            @Override
            public void updateDoubleViewMode(RecordView.DoubleViewMode mode) {
                if (mode == RecordView.DoubleViewMode.None){
                    mRenderPool.runSync(new Runnable() {
                        @Override
                        public void run() {
                            if (mFP.getFilter(RecordView.DOUBLE_VIEW_INDEX) != null){
                                mFP.deleteFilter(RecordView.DOUBLE_VIEW_INDEX);
                                mCurrentAudioPath = "";
                                CURRENT_MAX_RECORD_DURATION = 15000;
                                MAX_RECORD_DURATION = 15000;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecordView.updateMinPosition(Constants.MIN_RECORDING_TIME / 15.f);
                                    }
                                });
                            }

                        }
                    });
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
                        updateDoubleView(mCurrentDoubleViewVideoPath);
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
        });


    }

    private void initFilterPipe() {

        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                mGLCtx = new GLContext();

                mGLCtx.createForRender(Engine.getInstance().getMainGLContext().getEGLContext());

                mGLCtx.makeCurrent();

                mOutputSurface = new OutputSurface();

                mOutputSurface.create(mGLCtx);

                mFP = new FilterPipe();

                isInitSuccess = mFP.create();
            }
        });

        mRecordPool.runSync(new Runnable() {
            @Override
            public void run() {
                mGLCtx.makeCurrent();

                mAudioRecord = new AudioRecord();
            }
        });
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
//                Log.e("ss", "setPreviewCallback onPreviewFrame  ");
//
//                if (mFP != null){
//                    Camera.Size size = camera.getParameters().getPreviewSize();
//                    Log.e("ss", "setPreviewCallback onPreviewFrame "+ size.width + "x" + size.height);
//
//                    // front rotation == -90 isflip == true
//                    // back rotation == 90 isflip == false
//
//
//                    mFP.updateDetectBuffer(data,size.width,size.height,size.width,-90);
//                }
//
//                camera.addCallbackBuffer(data);
//            }
//        });

//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//
//                Log.e("ss", "setPreviewCallback onPreviewFrame  ");
//
//                if (mFP != null){
//                    Camera.Size size = camera.getParameters().getPreviewSize();
//                    Log.e("ss", "setPreviewCallback onPreviewFrame "+ size.width + "x" + size.height);
//
//                    // front rotation == -90 isflip == true
//                    // back rotation == 90 isflip == false
//
//
//                    mFP.updateDetectBuffer(data,size.width,size.height,size.width,-90);
//                }
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

                mRenderPool.runSync(new Runnable() {
                    @Override
                    public void run() {
                        Image input = new Image(data.image,System.currentTimeMillis());

                        boolean enableMarkSence = false;
                        if (mFP.getFilter(RecordView.mFilterMap.get(SelesParameters.FilterModel.Reshape)) != null
                                || mFP.getFilter(RecordView.mFilterMap.get(SelesParameters.FilterModel.CosmeticFace)) != null){
                            enableMarkSence = mRecordView.checkEnableMarkSence();
                        }
                        input.setMarkSenceEnable(enableMarkSence);
                        double agree = mOrientationListener.getDeviceAngle() + InterfaceOrientation.Portrait.getDegree();
                        input.setAgree(agree);
                        Image output = mFP.process(input);
                        mShotPhoto = output.toBitmap();
                    }
                });

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
        mRenderPool.runSync(new Runnable() {
            @Override
            public void run() {
                mCameraView.release();
                mFP.clearFilters();
                mFP.destroy();
                Engine.getInstance().release();
            }
        });
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

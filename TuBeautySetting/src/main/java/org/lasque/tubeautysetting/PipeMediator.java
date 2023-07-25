package org.lasque.tubeautysetting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.service.dreams.DreamService;
import android.text.TextUtils;
import android.util.Size;
import android.util.SizeF;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.tusdk.pulse.filter.FilterDisplayView;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.filter.detector.DetectResult;
import com.tusdk.pulse.filter.detector.Detector;
import com.tusdk.pulse.filter.detector.DetectorBuffer;

import org.lasque.tusdkpulse.core.struct.TuSdkSize;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.image.ImageOrientation;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/20  17:53
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class PipeMediator implements ImageConvert.ProcessProperty {

    private static final String TAG = "PipeMediator";

    private static PipeMediator INSTANCE = null;

    public static PipeMediator getInstance() {
        if (INSTANCE == null) {
            synchronized (PipeMediator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PipeMediator();
                }
            }
        }
        return INSTANCE;
    }


    private PipeMediator() {
        mRenderPipe = new RenderPipe();
        mRenderPipe.initRenderPipe();
        mImageConvert = new ImageConvert(mRenderPipe);
    }

    /**
     * 输入纹理尺寸
     */
    private TuSdkSize mInputSize;

    /**
     * 相机纹理输入方向
     */
    private ImageOrientation mCameraInputOrientation;

    /**
     * RenderPipe
     */
    private RenderPipe mRenderPipe;

    /**
     * 渲染宽度,默认值为720
     */
    private int mRenderWidth = 1080;

    private int mBufferOrientation = 90;
    private boolean mIsBufferFlip = true;

    /**
     * 渲染比例
     */
    private Pair<Double, Double> mAspect;

    /**
     * 当前渲染区域
     */
    private RectF mCurrentPreviewRect;

    /**
     * 方向传感器
     */
    private SensorHelper mSensorHelper;

    /**
     * 渲染管理器
     */
    private ImageConvert mImageConvert;

    /**
     * 美颜参数管理器
     */
    private BeautyManager mBeautyManager;

    /**
     * 预览渲染管理器
     */
    private PreviewManager mPreviewManager;

    private Detector mDetector;

    private boolean isReady = false;

    /**
     * --------------------------- Record Manager -----------------------------------
     */

    private RecordManager mRecordManager = new RecordManager();

    private long mRecordingStart = -1;

    private double mVideoStretch = 1.0;

    private double mAudioStretch = 1.0;

    private Thread mAudioConvertReceiverThread;

    private Thread mAudioWritingThread;

    private AudioConvert mAudioConvert;

    private boolean isNeedMixer = false;

    private boolean isNeedAudioPrecess = false;

    private String mBGMPath = "";

    private Long mAudioStartPos = 0L;

    private AudioConvert.AudioPitchType mCurrentAudioPitch = AudioConvert.AudioPitchType.NORMAL;

    private LinkedBlockingQueue<AudioConvert.AudioItem> mOutputQueue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

    private Runnable mAudioConvertReceiverRunnable = new Runnable() {
        @Override
        public void run() {
            while (!ThreadHelper.isInterrupted()) {
                try {
                    AudioConvert.AudioItem item = mAudioConvert.receiveAudioData();
                    if (item == null) continue;
                    if (item.length > 0) {
                        long currentAudioTime = System.currentTimeMillis() - mRecordingStart;
                        mOutputQueue.put(item);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable mAudioWriterRunnable = new Runnable() {
        @Override
        public void run() {
            while (!ThreadHelper.isInterrupted()) {
                AudioConvert.AudioItem item = mOutputQueue.poll();
                if (item != null) {
                    TLog.e("[Debug] current audio info %s", item);
                    mRecordManager.sendAudio(item.buffer, item.buffer.length, item.time);
                }
            }
        }
    };

    /**
     * @return 是否可用
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * @param renderWidth 渲染画面宽度 默认值为720
     */
    public void setRenderWidth(int renderWidth) {
        mRenderWidth = renderWidth;
        if (mImageConvert.isReady()) {
            mImageConvert.setRenderWidth(renderWidth);
        }
    }

    /**
     * @param orientation 设置Buffer旋转角度
     */
    public void setBufferOrientation(int orientation){
        this.mBufferOrientation = mBufferOrientation;
    }

    /**
     * @param isFlip 设置Buffer是否为翻转状态
     */
    public void setIsBufferFlip(boolean isFlip){
        this.mIsBufferFlip = mIsBufferFlip;
    }

    /**
     * @param context Context 对象
     * @param parent  渲染View父布局
     * @param aspect  默认渲染尺寸比例
     * @return Init结果 不为0时 说明参数存在错误
     */
    public Pair<Boolean, Integer> requestInit(Context context, ViewGroup parent, SizeF aspect) {
        synchronized (PipeMediator.class){

            TLog.e("Pipe request init start");

            if (isReady) {
                return new Pair<>(false, -1);
            }



            mSensorHelper = new SensorHelper(context);

            mAspect = new Pair<>(((double) aspect.getWidth()), ((double) aspect.getHeight()));

            if (mInputSize == null) return new Pair<>(false, -2);


            mImageConvert.setInputSize(mInputSize.width, mInputSize.height, mCameraInputOrientation);
            mImageConvert.setRenderWidth(mRenderWidth);
            mImageConvert.setAspect(aspect.getWidth(), aspect.getHeight());
            mImageConvert.setProcessProperty(this);

            Pair<Boolean, Integer> result = mImageConvert.requestInit();
            if (!result.first) {
                return result;
            }

            mAudioConvert = new AudioConvert();

            mBeautyManager = new BeautyManager();
            mBeautyManager.requestInit(mRenderPipe);

            mDetector = new Detector(Detector.DetectorType.BUFFER,mBeautyManager.getContext());

            mPreviewManager = new PreviewManager();
            result = mPreviewManager.requestInit(context, parent);
            if (!result.first) {
                return result;
            }

            isReady = true;

            return new Pair<Boolean, Integer>(true, 0);

        }
    }

    /**
     * @param aspect 更新渲染画面比例
     */
    public void updateAspect(SizeF aspect) {
        if (!isReady) return;
        mAspect = new Pair<>(((double) aspect.getWidth()), ((double) aspect.getHeight()));
        mImageConvert.updateAspect(aspect.getWidth(), aspect.getHeight());
    }

    /**
     * @param rectF 实际渲染区域 默认计算方式为fitin
     */
    public void changedRect(@Nullable RectF rectF) {
        mCurrentPreviewRect = rectF;
    }

    /**
     * @return SurfaceTexture Android系统纹理包装类 OES类型 可直接设置到Camera中
     */
    public SurfaceTexture getSurfaceTexture() {
        return mImageConvert.getSurfaceTexture();
    }

    /**
     * SurfaceTexture 回调中调用此方法 通知管线进行渲染
     */
    public Image onFrameAvailable() {
        if (!isReady) return null;
        synchronized (PipeMediator.class) {
            //1. 通过ImageConvert 将Android默认的OES纹理转换为可处理的Texture2D纹理
            TLog.e("Pipe image onFrameAvailable start");
            Image in = mImageConvert.onFrameAvailable();
            //2. 通过BeautyManager进行美颜处理
            TLog.e("Pipe image processFrame start");
//            Image out = mBeautyManager.processFrame(in);
        Image out = in;
            //3. 将处理后的Image显示到View上
            TLog.e("Pipe image preview start");
            if (mCurrentPreviewRect == null) {
                mPreviewManager.updateImage(out);
            } else {
                mPreviewManager.updateImage(out, mCurrentPreviewRect);
            }
            //4. 需要录制的情况下 将处理后的Image对象送入RecordManager进行文件输出
            if (mRecordManager != null) {
                long recordPos = System.currentTimeMillis();
                mRecordManager.sendImage(out, recordPos);
            }
            TLog.e("Pipe image release start");
            in.release();
//            out.release();
            return out;
        }
    }

    private long frameCount = 0;
    private long frameDurationSum = 0;
    private long frameImageConvertSum = 0;
    private long frameProcessSum = 0;

    public Image onFrameAvailable(ByteBuffer buffer, int bufferWidth, int bufferHeight, int stride){
        if (!isReady) return null;
        synchronized (PipeMediator.class) {
            //1. 通过ImageConvert 将Android默认的OES纹理转换为可处理的Texture2D纹理

            long renderStart = System.currentTimeMillis();

            TLog.e("Pipe image onFrameAvailable start");

            long convertStart = System.currentTimeMillis();
            Image in = mImageConvert.onFrameAvailable();
            long convertOver = System.currentTimeMillis() - convertStart;
            frameImageConvertSum += convertOver;
            //2. 通过BeautyManager进行美颜处理
            TLog.e("Pipe image processFrame start");

            Size textureSize = new Size(in.GetWidth(),in.GetHeight());
            DetectorBuffer detectorBuffer = DetectorBuffer.makeFromBuffer(buffer,bufferWidth,bufferHeight,stride,
                    textureSize,
                    mBufferOrientation,mIsBufferFlip,4,
                    System.currentTimeMillis());

            DetectResult result = mDetector.detect(detectorBuffer);

            long processStart = System.currentTimeMillis();
            Image out = mBeautyManager.processFrame(in,result);
            long processOver = System.currentTimeMillis() - processStart;
            frameProcessSum += processOver;
//        Image out = in;

            //3. 将处理后的Image显示到View上
            TLog.e("Pipe image preview start");
            if (mCurrentPreviewRect == null) {
                mPreviewManager.updateImage(out);
            } else {
                mPreviewManager.updateImage(out, mCurrentPreviewRect);
            }
            //4. 需要录制的情况下 将处理后的Image对象送入RecordManager进行文件输出
            if (mRecordManager != null) {
                long recordPos = System.currentTimeMillis();
                mRecordManager.sendImage(out, recordPos);
            }
            TLog.e("Pipe image release start");
            in.release();
            out.release();

            detectorBuffer.release();
            result.release();

//            finalOut.release();

            long renderOver = System.currentTimeMillis() - renderStart;
            frameDurationSum += renderOver;
            frameCount++;
            if (frameCount % 100 == 0){
                TLog.e("PULSE_ENABLE_PERFORMANCE_TEST [All-Render-Duration] all : %d convert : %d process : %d",frameDurationSum / frameCount,frameImageConvertSum / frameCount,frameProcessSum / frameCount);
            }

//            if (renderOver > 40){
//                TLog.e("PULSE_ENABLE_PERFORMANCE_TEST [Slow] all : %d convert : %d process : %d aspect : %d release : %d",renderOver,convertOver,processOver,aspectOver,releaseOver);
//
//            }

            return out;
        }
    }

    /**
     * 开始录制视频
     *
     * @param outputPath   视频输出绝对路径
     * @param width        视频输出宽度
     * @param height       视频输出高度
     * @param watermark    水印图片
     * @param watermarkPos 水印位置 // 0 : tl, 1 : tr, 2 : bl, 3 : br
     */
    public void startRecord(String outputPath, int width, int height, @Nullable Bitmap watermark, int watermarkPos, RecordManager.RecordListener listener, boolean isSaveToAlbum, @Nullable String albumName, long repeatDuration) {
        mRecordManager.setRecordListener(listener);
        mRecordManager.newExporter(outputPath, width, height, 2, 44100, watermark, watermarkPos, mRenderPipe.getContext(), isSaveToAlbum, albumName);

        if (isNeedMixer || isNeedAudioPrecess) {
            mAudioConvert.requestInit(2, 44100);
            mAudioConvert.updateAudioPitch(mCurrentAudioPitch);
            mAudioConvert.updateAudioStretch(mAudioStretch);
            if (isNeedMixer)
                mAudioConvert.initAudioMixer(mBGMPath, mAudioStartPos, repeatDuration);
            mAudioConvertReceiverThread = ThreadHelper.runThread(mAudioConvertReceiverRunnable);
            mAudioConvert.startAudioConvert();
        }

        mRecordManager.startExporter();

        mAudioWritingThread = ThreadHelper.runThread(mAudioWriterRunnable);
    }

    /**
     * 暂停录制
     */
    public void pauseRecord() {
        mRecordManager.pauseExporter();

        if (isNeedMixer || isNeedAudioPrecess) {
            mAudioConvert.stopAudioConvert();
            mAudioConvert.release();
            mAudioConvertReceiverThread.interrupt();
        }

        mAudioWritingThread.interrupt();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        mRecordManager.stopExporter();
    }

    /**
     * @param speed 录制速率 0.1~2.0
     */
    public void setRecordSpeed(double speed) {
        isNeedAudioPrecess = true;
        mAudioStretch = speed;
        if (mAudioStretch == 2.0) {
            mVideoStretch = 0.5;
        } else if (mAudioStretch == 1.5) {
            mVideoStretch = 0.75;
        } else if (mAudioStretch == 0.75) {
            mVideoStretch = 1.5;
        } else if (mAudioStretch == 0.5) {
            mVideoStretch = 2.0;
        } else {
            mVideoStretch = mAudioStretch;
            isNeedAudioPrecess = false;
        }

        mRecordManager.updateStretch(mAudioStretch);
    }

    /**
     * @return 删除当前最后一段录制片段
     */
    public RecordManager.VideoFragmentItem popLastFragment() {
        return mRecordManager.popFragment();
    }

    /**
     * @return 获取当前录制长度
     */
    public long getCurrentRecordDuration() {
        return mRecordManager.getCurrentRecordDuration();
    }

    /**
     * @return 当前录制片段数量
     */
    public int getCurrentRecordFragmentSize() {
        return mRecordManager.getFragmentSize();
    }


    /**
     * @param durationMS 录制最大时间
     */
    public void setMaxRecordDuration(long durationMS) {
        mRecordManager.setMaxRecordDuration(durationMS);
    }

    /**
     * @param isPlay 是否开始播放合拍片段
     */
    public void setJoinerPlay(boolean isPlay) {
        if (!hasJoiner()) return;
        mBeautyManager.setJoinerPlayerState(isPlay);
        if (isPlay) {
            mAudioConvert.startAudioPlayer();
        } else {
            mAudioConvert.stopAudioPlayer();
        }

    }

    public void setBGMPlay(boolean isPlay) {
        if (TextUtils.isEmpty(mBGMPath)) return;
        if (isPlay) {
            mAudioConvert.startAudioPlayer();
        } else {
            mAudioConvert.stopAudioPlayer();
        }
    }


    /**
     * @param buffer 音频PCM数据
     * @param size   数据长度
     * @param ts     时间戳
     * @return
     */
    public boolean sendAudioBuffer(byte[] buffer, int size, long ts) {
        AudioConvert.AudioItem audioItem = new AudioConvert.AudioItem(buffer, size, ts);
        if (!isNeedAudioPrecess && !isNeedMixer) {
            try {
                mOutputQueue.put(audioItem);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            mAudioConvert.sendAudioData(audioItem);
        }

        return true;
    }


    @Override
    public double getAngle() {
        return mSensorHelper.getDeviceAngle();
    }

    @Override
    public boolean getEnableMarkSence() {
        return mBeautyManager.checkEnableMarkSence();
    }

    /**
     * @return 获取美颜属性管理类
     */
    public BeautyManager getBeautyManager() {
        return mBeautyManager;
    }

    /**
     * 设置视频画面输入大小
     *
     * @param width  宽度
     * @param height 高度
     */
    public void setInputSize(int width, int height) {
        mInputSize = new TuSdkSize(width, height);
    }

    /**
     * 设置视频画面输入方向
     *
     * @param rotation 角度
     * @param isMirror 是否镜像
     */
    public void setInputRotation(int rotation, boolean isMirror) {
        mCameraInputOrientation = ImageOrientation.getValue(rotation, isMirror);
    }

    /**
     * @param pitchType 变声类型
     */
    public void setSoundPitchType(AudioConvert.AudioPitchType pitchType) {
        mCurrentAudioPitch = pitchType;
        if (pitchType != AudioConvert.AudioPitchType.NORMAL)
            isNeedAudioPrecess = true;
        else
            isNeedAudioPrecess = false;
    }

    /**
     * @param path 背景音乐路径
     */
    public void setBGM(String path, Long audioStartPos) {
        mBGMPath = path;
        mAudioStartPos = audioStartPos;
        isNeedAudioPrecess = true;
        isNeedMixer = true;
    }

    /**
     * @param videoPath  合拍视频路径
     * @param cameraRect 合拍中相机渲染区域
     * @param videoRect  合拍中视频渲染区域
     */
    public void setJoiner(String videoPath, String audioPath, RectF cameraRect, RectF videoRect, Long audioStartPos, boolean useSoftDecoding, RectF videoSrcRect, RectF cameraSrcRect) {
        if (!TextUtils.isEmpty(videoPath)) {
            mBeautyManager.updateVideoStretch(mVideoStretch);
            mBeautyManager.setRenderSize(mImageConvert.getRenderSize());
            mBeautyManager.setJoiner(videoRect, cameraRect, videoPath, useSoftDecoding, videoSrcRect, cameraSrcRect);
        }

        if (!TextUtils.isEmpty(audioPath)) {
            mBGMPath = audioPath;
            mAudioStartPos = audioStartPos;
            isNeedAudioPrecess = true;
            isNeedMixer = true;
        }
    }


    /**
     * 删除合拍
     */
    public void deleteJoiner() {
        mBGMPath = "";
        mAudioConvert.resetAudioMixer();
        mBeautyManager.deleteJoiner();
        isNeedMixer = false;
    }

    /**
     * 合拍画面跳转
     *
     * @param ts 时间戳
     * @return
     */
    public boolean joinerSeek(long ts) {
        return mBeautyManager.joinerSeek(ts);
    }

    /**
     * @return 当前是否在合拍状态
     */
    public boolean hasJoiner() {
        return mBeautyManager.hasJoiner();
    }

    public boolean hasBGM() {
        if (hasJoiner()) return false;
        return !TextUtils.isEmpty(mBGMPath);
    }

    /**
     * @param color 预览区域背景颜色
     */
    public void setPreviewBackgroundColor(int color) {
        mPreviewManager.setBackgroundColor(color);
    }

    /**
     * 处理相机拍照输出的图片
     *
     * @param input 单张图片
     * @return 处理后的图片
     */
    public Bitmap processBitmap(Bitmap input) {
        Size textureSize = new Size(input.getWidth(),input.getHeight());
        DetectorBuffer buffer = DetectorBuffer.makeFromBitmap(input,textureSize,0,false,System.currentTimeMillis());
        DetectResult result = mDetector.detect(buffer);

        Image in = new Image(input, System.currentTimeMillis());
        Image res = mBeautyManager.processFrame(in,result);
        Bitmap output = res.toBitmap();
        res.release();

        buffer.release();
        result.release();

        return output;
    }

    /**
     * 释放
     */
    public void release() {
        if (!isReady) return;
        synchronized (PipeMediator.class) {

            TLog.e("Pipe release -- 0");

            isReady = false;

            mPreviewManager.release();
            TLog.e("Pipe release -- 1");

            mBeautyManager.release();
            TLog.e("Pipe release -- 2");


            mImageConvert.release();
            TLog.e("Pipe release -- 3");

            mRenderPipe.release();
            TLog.e("Pipe release -- 4");

            mDetector.release();
            mDetector = null;

            INSTANCE = null;


            TLog.e("Pipe release -- 5");
        }

    }


    public FilterDisplayView getCurrentView() {
        return mPreviewManager.getCurrentView();
    }
}

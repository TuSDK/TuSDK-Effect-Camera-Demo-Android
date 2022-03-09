package org.lasque.tubeautysetting;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.tusdk.pulse.DispatchQueue;
import com.tusdk.pulse.filter.FileExporter;
import com.tusdk.pulse.filter.Image;
import com.tusdk.pulse.utils.gl.GLContext;

import org.lasque.tusdkpulse.core.TuSdkContext;
import org.lasque.tusdkpulse.core.utils.FileHelper;
import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;
import org.lasque.tusdkpulse.core.utils.TuSdkDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/23  16:58
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class RecordManager {

    public static class VideoFragmentItem{
        /**
         * 片段保存路径
         */
        public String path;
        /**
         * 片段长度
         */
        public long fragmentDuration;
    }

    public static interface RecordListener{

        /**
         * 进度回调
         * @param progress 比例 0~1
         * @param ts 时间戳
         */
        void onProgress(float progress,long ts);

        /**
         * 录制超时回调
         */
        void onRecordTimeOut();

        /**
         * 启动录制回调
         */
        void onRecordStart();

        /**
         * 录制暂停回调
         */
        void onRecordPause();

        /**
         * 录制停止回调
         */
        void onRecordStop();

    }

    private RecordListener mRecordListener;

    private boolean isRecordingTimeOut = false;

    private FileExporter mExporter;

    private String mOutputPath;

    private FileExporter.Config mCurrentConfig;

    private VideoFragmentItem mCurrentFragment;

    private List<VideoFragmentItem> mCurrentFragmentList = new LinkedList<>();;

    private long mRecordingStart = 0;

    private boolean isRecording = false;

    private long mLastFrameTs = 0;

    private DispatchQueue mWritenQueue;

    private double mCurrentStretch = 1.0;

    private long mMaxRecordDurationMS = 15000;

    private long mCurrentRecordDurationMS = 0;

    /**
     * @param listener 录制监听
     */
    public void setRecordListener(RecordListener listener){
        mRecordListener = listener;
    }

    /**
     * @param durationMS 录制最大时间
     */
    public void setMaxRecordDuration(long durationMS){
        mMaxRecordDurationMS = durationMS;
    }

    /**
     * 创建视频录制工具
     *
     * @param outputPath 最终输出路径
     * @param width 视频宽度
     * @param height 视频高度
     * @param channels 音频声道数
     * @param sampleRate 音频采样率
     * @param watermark 水印图片
     * @param watermarkPos 水印位置
     * @param glContext 纹理采集环境上下文
     * @return 创建状态 -1 参数异常 0 成功
     */
    public Pair<Boolean,Integer> newExporter(String outputPath, int width, int height, int channels, int sampleRate, @Nullable Bitmap watermark, int watermarkPos, GLContext glContext){
        if (width <= 0 || height <= 0 || channels > 2 || channels < 1 || sampleRate < 0){
            return new Pair<>(false,-1);
        }

        mOutputPath = outputPath;

        mCurrentConfig = new FileExporter.Config();
        mCurrentConfig.width = width / 2 * 2;
        mCurrentConfig.height = height / 2 * 2;
        mCurrentConfig.channels = channels;
        mCurrentConfig.sampleRate = sampleRate;
        mCurrentConfig.framerate = 30;

        if (watermark != null){
            mCurrentConfig.watermark = watermark;
            mCurrentConfig.watermarkPosition = watermarkPos;
        }




        mWritenQueue = new DispatchQueue();
        mWritenQueue.runSync(new Runnable() {
            @Override
            public void run() {
                glContext.makeCurrent();
            }
        });
        return new Pair<>(true,0);
    }

    /**
     * 重置录制工具状态
     */
    public void resetExporter(){
        mOutputPath = "";
        mCurrentConfig = null;
    }

    /**
     * @return 开始视频录制
     */
    public boolean startExporter(){
        if (mExporter != null) return false;
        if (mRecordingStart == 0) mRecordingStart = System.currentTimeMillis();
        mCurrentFragment = new VideoFragmentItem();
        mCurrentFragment.path = getTempOutputPath();

        TLog.e("[Debug] current output path %s",mCurrentFragment.path);

        mWritenQueue.runSync(new Runnable() {
            @Override
            public void run() {
                mExporter = new FileExporter();
                mCurrentConfig.savePath = mCurrentFragment.path;
                boolean res = mExporter.open(mCurrentConfig);

                if (res){
                    mCurrentFragmentList.add(mCurrentFragment);
                }
                isRecording = res;
            }
        });

        if (mRecordListener != null){
            mRecordListener.onRecordStart();
        }

        return isRecording;
    }

    /**
     * @return 暂停录制
     */
    public boolean pauseExporter(){
        if (!isRecording) return false;

        mWritenQueue.runSync(new Runnable() {
            @Override
            public void run() {
                isRecording = false;
                mExporter.close();

                mCurrentFragment.fragmentDuration = mLastFrameTs;
                mExporter = null;
                mRecordingStart = 0L;
                mCurrentRecordDurationMS += mCurrentFragment.fragmentDuration;

                ThreadHelper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRecordListener != null){
                            mRecordListener.onRecordPause();
                        }
                    }
                });
            }
        });


        return true;

    }

    /**
     * @return 停止录制
     */
    public boolean stopExporter(){

        String inputPath;
        if (mCurrentFragmentList.size() == 1){
            inputPath = mCurrentFragment.path;
        } else {
            String[] paths = new String[mCurrentFragmentList.size()];
            for (int i=0;i<mCurrentFragmentList.size();i++){
                paths[i] = mCurrentFragmentList.get(i).path;
            }
            inputPath = getTempOutputPath();
            FileExporter.MergeVideoFiles(inputPath,paths);
        }
        saveVideo(inputPath,mOutputPath);
        isRecordingTimeOut = false;
        mCurrentFragmentList.clear();
        mCurrentFragment = null;
        mCurrentRecordDurationMS = 0;
        mLastFrameTs = 0;

        if (mRecordListener != null){
            mRecordListener.onRecordStop();
        }
        return true;
    }

    /**
     * @return 弹出最后一段录制片段
     */
    public VideoFragmentItem popFragment(){
        if (mCurrentFragmentList.size() == 0) return null;

        isRecordingTimeOut = false;

        VideoFragmentItem item = mCurrentFragmentList.get(mCurrentFragmentList.size() - 1);
        mCurrentFragmentList.remove(item);
        mCurrentRecordDurationMS -= item.fragmentDuration;

        return item;
    }

    /**
     * @return 获取当前录制片段数量
     */
    public int getFragmentSize(){
        return mCurrentFragmentList.size();
    }

    /**
     * @return 获取当前录制时长
     */
    public long getCurrentRecordDuration(){
        return mCurrentRecordDurationMS;
    }

    /**
     * 输入视频
     * @param image 视频帧
     * @param ts 时间戳
     */
    public void sendImage(Image image,long ts){
        if (!isRecording) return;
        if (isRecordingTimeOut) return;
        mLastFrameTs = ts - mRecordingStart;
        mLastFrameTs *= mCurrentStretch;
        final long writeFrameTs = mLastFrameTs;
        long currentDuration = (mCurrentRecordDurationMS + mLastFrameTs);
        float p = currentDuration / (float)mMaxRecordDurationMS;
        mWritenQueue.runAsync(new Runnable() {
            @Override
            public void run() {
                mExporter.sendImage(image,writeFrameTs);
                image.release();
                TLog.e("[Debug] record manager send image in ts %s p %s",ts,p);

                ThreadHelper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRecordListener != null){
                            mRecordListener.onProgress(p,currentDuration);
                        }
                    }
                });
            }
        });

        TLog.e("[Debug] record manager send image out ts %s p %s",ts,p);

        if (p >= 1){
            isRecordingTimeOut = true;

            if (mRecordListener != null){
                mRecordListener.onRecordTimeOut();
            }

            pauseExporter();



        }
    }

    /**
     * 输入音频 最大可处理4096长度的数据
     * @param buffer 音频数据
     * @param length 数据长度
     * @param ts 时间戳
     */
    public void sendAudio(byte[] buffer,int length,long ts){
        if (!isRecording) return;
        if (isRecordingTimeOut) return;
        mExporter.sendAudioData(buffer,length, (long) (ts * mCurrentStretch));
    }

    /**
     * @param stretch 更新视频速度
     */
    public void updateStretch(double stretch){
        mCurrentStretch = stretch;
    }

    private String getTempOutputPath(){
        String tempPath = TuSdkContext.getAppCacheDir("recordCache",false).getAbsolutePath() + "/camera_temp"+System.currentTimeMillis()+".mp4";
        return tempPath;
    }

    /**
     * 保存视频 目前适配到Android11
     * @param input 输入路径
     * @param output 输出路径 可以为公共区域
     */
    public void saveVideo(String input,String output){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            long now = TuSdkDate.create().getTimeInMillis();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN,now);
            values.put(MediaStore.Images.Media.DATE_MODIFIED,now / 1000);
            values.put(MediaStore.Images.Media.DATE_ADDED,now / 1000);
            values.put(MediaStore.Images.Media.DISPLAY_NAME,output.substring(output.lastIndexOf("/")));
            values.put(MediaStore.Images.Media.MIME_TYPE,"video/mp4");
            values.put(MediaStore.MediaColumns.IS_PENDING,1);
            Uri uri = TuSdkContext.context().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);
            try {
                OutputStream outputStream = TuSdkContext.context().getContentResolver().openOutputStream(uri);
                File inputFile = new File(input);
                InputStream inputStream = FileHelper.getFileInputStream(inputFile);
                FileHelper.copy(inputStream,outputStream);
                FileHelper.safeClose(inputStream);
                FileHelper.safeClose(outputStream);
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING,0);
                TuSdkContext.context().getContentResolver().update(uri,values,null,null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            FileHelper.rename(new File(input),new File(output));

            ThreadHelper.runThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(new File(output));
                    intent.setData(uri);
                    TuSdkContext.context().sendBroadcast(intent);
                }
            });
        }
    }


}

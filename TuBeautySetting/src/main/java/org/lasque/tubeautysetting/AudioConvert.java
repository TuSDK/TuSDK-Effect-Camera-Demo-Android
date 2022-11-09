package org.lasque.tubeautysetting;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.tusdk.pulse.Config;
import com.tusdk.pulse.MediaInspector;
import com.tusdk.pulse.audio.AudioPipe;
import com.tusdk.pulse.audio.AudioProcessor;
import com.tusdk.pulse.audio.AudioSamples;
import com.tusdk.pulse.audio.processors.AudioPitchProcessor;
import com.tusdk.pulse.audio.processors.AudioStretchProcessor;
import com.tusdk.pulse.filter.FileRecordAudioMixer;

import org.lasque.tusdkpulse.core.utils.TLog;
import org.lasque.tusdkpulse.core.utils.ThreadHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * TuSDK
 * org.lasque.tubeautysetting
 * android-ec-demo
 *
 * @author H.ys
 * @Date 2021/12/27  14:38
 * @Copyright (c) 2020 tusdk.com. All rights reserved.
 */
public class AudioConvert {

    public static class AudioItem {

        public byte[] buffer;
        public int length;
        public long time;

        public AudioItem(byte[] buffer, int length, long time) {
            this.buffer = buffer;
            this.length = length;
            this.time = time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AudioItem)) return false;
            AudioItem audioItem = (AudioItem) o;
            return length == audioItem.length && time == audioItem.time && Arrays.equals(buffer, audioItem.buffer);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(length, time);
            result = 31 * result + Arrays.hashCode(buffer);
            return result;
        }

        @Override
        public String toString() {
            return "AudioItem{" +
                    "length=" + length +
                    ", time=" + time +
                    '}';
        }
    }

    public enum AudioPitchType {

        NORMAL(AudioPitchProcessor.TYPE_NORMAL),
        MONSTER(AudioPitchProcessor.TYPE_MONSTER),
        UNCLE(AudioPitchProcessor.TYPE_UNCLE),
        GIRL(AudioPitchProcessor.TYPE_GIRL),
        LOLITA(AudioPitchProcessor.TYPE_LOLITA);
        public String type;

        private AudioPitchType(String type) {
            this.type = type;
        }
    }


    private static final String TAG = "AudioConvert";

    private boolean isReady = false;


    private boolean isNeedMixer = false;

    private double mAudioStretch = 1.0;

    private String mMixFilePath = "";

    private String mPitchType = AudioPitchType.NORMAL.type;

    private boolean isNeedAudioPlay = true;

    private boolean isAudioPlayerPause = true;

    private boolean isAudioPlayerStop = false;

    private boolean mAudioPlayThreadAlive = true;


    private LinkedBlockingQueue<AudioConvert.AudioItem> mOutputQueue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

    private LinkedBlockingQueue<AudioConvert.AudioItem> mInputQueue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

    private FileRecordAudioMixer mAudioMixer;

    private AudioPipe mAudioPipe;

    final private int PITCH_INDEX = 10;
    final private int STRETCH_INDEX = 20;

    private int mBufferSize = 0;

    private AudioTrack mCurrentAudioTrack;

    private Thread mPipeInputThread;

    private Runnable mPipeInputRunnable = new Runnable() {
        @Override
        public void run() {
            while (!ThreadHelper.isInterrupted()) {

                AudioItem item = null;
                item = mInputQueue.poll();
                if (item != null) {
                    if (item.time < 0) break;
                    synchronized (AudioPipe.class){
                        AudioSamples audioSamples = new AudioSamples(item.buffer, 1024, 2, 44100, item.time);
                        TLog.e("[Debug] %s is audio samples init %s length %s",TAG,audioSamples.isInit(),item.buffer.length);
                        if (audioSamples.isInit()) {
                            while (!mAudioPipe.sendAudioSamples(audioSamples)){

                            }
                            TLog.e("[Debug] %s send audio samples to audio pipe",TAG);
                        }
                    }
                }

            }
        }
    };

    private Thread mPipeOutputThread;

    private Runnable mPipeOutputRunnable = new Runnable() {
        @Override
        public void run() {
            while (!ThreadHelper.isInterrupted()) {
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                synchronized (AudioPipe.class){
                    if (mAudioPipe == null) return;
                    boolean res = mAudioPipe.receiveAudioSamples(buffer, bufferSize);
                    if (res) {
//                    TLog.e("[Debug] %s receive audio samples to audio pipe res %s",TAG,res);
                        long currentAudioTime = System.currentTimeMillis();
                        AudioItem audioItem = new AudioItem(buffer, bufferSize, currentAudioTime);
                        if (isNeedMixer) {
                            int ret = mAudioMixer.sendPrimaryAudio(audioItem.buffer, audioItem.length);
                            TLog.e("[Debug] %s send primary audio to audio mixer",TAG);
                            if (ret == -4) {
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            try {
                                mOutputQueue.put(audioItem);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }
        }
    };

    private Thread mMixedOutputThread;

    private Runnable mMixerOutputRunnable = new Runnable() {
        @Override
        public void run() {
            while (isNeedMixer && !ThreadHelper.isInterrupted()) {
                byte[] buffer = new byte[mBufferSize];
                int res = -2;
                res = mAudioMixer.getPCMForRecord(buffer, mBufferSize);
                TLog.e("[Debug] %s get PCM for record from audio mixer ret %s",TAG,res);
                if (res < 0) {
                    if (res == -2) {
                        AudioItem audioItem = new AudioItem(null, -1, -1);
                        try {
                            mOutputQueue.put(audioItem);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else continue;
                } else {
                    AudioItem audioItem = new AudioItem(buffer, mBufferSize, System.currentTimeMillis());
                    try {
                        mOutputQueue.put(audioItem);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };


    private Thread mCurrentAudioPlayerThread;


    /**
     * @param channels 声道数
     * @param sampleRate 音频采样率
     * @return 初始化状态
     */
    public boolean requestInit(int channels, int sampleRate) {
        if (isReady) return false;
        mAudioPipe = new AudioPipe();
        AudioPipe.Config config = new AudioPipe.Config();
        config.sampleRate = sampleRate;
        config.channels = channels;
        boolean res = mAudioPipe.create(config);
        if (res) {
            AudioProcessor audioPitchProcessor = new AudioProcessor(mAudioPipe.getContext(), AudioPitchProcessor.TYPE_NAME);
            Config pitchConfig = new Config();
            pitchConfig.setString(AudioPitchProcessor.CONFIG_PITCH_TYPE, AudioPitchProcessor.TYPE_NORMAL);
            audioPitchProcessor.setConfig(pitchConfig);
            mAudioPipe.addProcessor(PITCH_INDEX, audioPitchProcessor);


            AudioProcessor audioStretchProcessor = new AudioProcessor(mAudioPipe.getContext(), AudioStretchProcessor.TYPE_NAME);
            Config stretchConfig = new Config();
            stretchConfig.setNumber(AudioStretchProcessor.CONFIG_STRETCH, mAudioStretch);
            audioStretchProcessor.setConfig(stretchConfig);
            mAudioPipe.addProcessor(STRETCH_INDEX, audioStretchProcessor);



            isReady = true;
        }

        int audioBitWidth = AudioFormat.ENCODING_PCM_16BIT;
        int channelConfig = (channels == 1) ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int minBufferSize = android.media.AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioBitWidth);
        int inputBufferSize = minBufferSize * 4;
        int frameSizeInBytes = channels * 2;
        mBufferSize = inputBufferSize / frameSizeInBytes * frameSizeInBytes;
        return res;
    }

    /**
     * @param path 混音文件路径
     * @param startPos 混音文件开始播放位置
     */
    public void initAudioMixer(String path, Long startPos,Long repeatDuration) {
        MediaInspector inspector = MediaInspector.shared();
        MediaInspector.MediaInfo info = inspector.inspect(path);
        for (MediaInspector.MediaInfo.AVItem stream : info.streams) {
            TLog.e("[Debug] %s init audio mixer stream %s",TAG,stream);
            if (stream instanceof MediaInspector.MediaInfo.Audio) {
                mAudioMixer = new FileRecordAudioMixer();
                Config config = new Config();
                config.setString(FileRecordAudioMixer.CONFIG_PATH, path);
                config.setNumber(FileRecordAudioMixer.CONFIG_CHANNELS, 2);
                config.setNumber(FileRecordAudioMixer.CONFIG_SAMPLE_RATE, 44100);
                config.setNumber(FileRecordAudioMixer.CONFIG_AUDIO_STRETCH, 1.0);
                config.setNumber(FileRecordAudioMixer.CONFIG_FILE_MIX_WEIGHT, 0.5);
                if (startPos > 0) {
                    config.setNumber(FileRecordAudioMixer.CONFIG_START_POS, startPos);
                }
                if (repeatDuration > 0){
                    config.setNumber(FileRecordAudioMixer.CONFIG_REPEAT_DURATION,repeatDuration);

                }
                boolean res = mAudioMixer.open(config);

                mMixedOutputThread = ThreadHelper.runThread(mMixerOutputRunnable);
                isNeedMixer = true;
                break;
            }
        }

    }

    /**
     * 开启音频处理
     */
    public void startAudioConvert(){
        mPipeInputThread = ThreadHelper.runThread(mPipeInputRunnable);
        mPipeOutputThread = ThreadHelper.runThread(mPipeOutputRunnable);
    }

    /**
     * 结束音频处理
     */
    public void stopAudioConvert(){
        mPipeInputThread.interrupt();
        mPipeOutputThread.interrupt();
    }

    /**
     * 结束混音文件播放
     */
    public void stopAudioPlayer(){
        isAudioPlayerPause = true;
        isAudioPlayerStop = true;


        if (mCurrentAudioPlayerThread != null) mCurrentAudioPlayerThread.interrupt();
    }

    /**
     * 开始混音文件播放
     */
    public void startAudioPlayer(){
        if (!isNeedAudioPlay) return;

        isAudioPlayerPause = false;
        isAudioPlayerStop = false;

        int channels = AudioFormat.CHANNEL_OUT_STEREO;

        int bufferSize = AudioTrack.getMinBufferSize(44100, channels, AudioFormat.ENCODING_PCM_16BIT);

        TLog.e("[Debug] %s audio player thread pre start",TAG);

        mCurrentAudioPlayerThread = ThreadHelper.runThread(new Runnable() {
            @Override
            public void run() {

                TLog.e("[Debug] %s audio player thread start",TAG);

                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, channels, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                audioTrack.setVolume(1f);

                mCurrentAudioTrack = audioTrack;

                audioTrack.play();

                byte[] buffer = new byte[bufferSize];

                while (!ThreadHelper.isInterrupted()) {

                    while (isAudioPlayerPause){
                        if (isAudioPlayerStop){
                            audioTrack.setVolume(0);
                            break;
                        }
                    }

                    if (isAudioPlayerStop){
                        break;
                    }
                    int res = -2;
                    if (mAudioMixer != null) {
                        res = mAudioMixer.getPCMForPlay(buffer, bufferSize);
                    }
                    if (res < 0) {
                        if (res == -2) {
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        int ret = audioTrack.write(buffer, 0, res);
                        TLog.e("[Debug] %s write audio buffer to audio track",TAG);
                    }
                }
                audioTrack.stop();
                audioTrack.release();
                mCurrentAudioTrack = null;
            }
        });
    }

    /**
     * 重置混音器
     */
    public void resetAudioMixer() {
        if (isNeedMixer) {
            isNeedMixer = false;
            mAudioMixer.close();
            mAudioMixer = null;
        }
    }


    public int sendAudioData(AudioItem item) {
        boolean res = mInputQueue.offer(item);
        TLog.e("[Debug] %s put audio item to input queue item info %s res %s",TAG,item,res);

        return res ? 0 : -1;
    }

    public AudioItem receiveAudioData() throws InterruptedException {
        AudioItem output = mOutputQueue.poll();
        return output;
    }

    public boolean updateAudioStretch(double audioStretch) {
        if (!isReady) return false;
        mAudioPipe.deleteProcessor(STRETCH_INDEX);
        AudioProcessor stretchProcessor = new AudioProcessor(mAudioPipe.getContext(), AudioStretchProcessor.TYPE_NAME);
        Config config = new Config();
        config.setNumber(AudioStretchProcessor.CONFIG_STRETCH, audioStretch);
        stretchProcessor.setConfig(config);
        boolean res = mAudioPipe.addProcessor(STRETCH_INDEX, stretchProcessor);
        return res;
    }

    public boolean updateAudioPitch(AudioPitchType type) {
        if (!isReady) return false;
        mAudioPipe.deleteProcessor(PITCH_INDEX);
        AudioProcessor pitchProcessor = new AudioProcessor(mAudioPipe.getContext(),AudioPitchProcessor.TYPE_NAME);
        Config config = new Config();
        config.setString(AudioPitchProcessor.CONFIG_PITCH_TYPE,type.type);
        pitchProcessor.setConfig(config);
        boolean res = mAudioPipe.addProcessor(PITCH_INDEX,pitchProcessor);
        return res;

    }

    public void release(){
        if (mPipeInputThread != null) {
            mPipeInputThread.interrupt();
        }
        if (mPipeOutputThread !=null){
            mPipeOutputThread.interrupt();
        }
        if (mCurrentAudioPlayerThread != null){
            mCurrentAudioPlayerThread.interrupt();
        }

        if (mMixedOutputThread != null) {
            mMixedOutputThread.interrupt();
        }

        resetAudioMixer();

        if (mAudioPipe != null){
            TLog.e("audio pipe release");
            mAudioPipe.destory();
            mAudioPipe = null;
        }



        mInputQueue.clear();
        mOutputQueue.clear();

        isReady = false;

    }

    public void setMute(boolean isMute){
        isNeedAudioPlay = !isMute;
    }

}

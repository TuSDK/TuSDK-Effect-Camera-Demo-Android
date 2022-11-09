/**
 *  TuSDK
 *  android-ec-demo$
 *  org.lasque.effectcamerademo.audio$
 *  @author  H.ys
 *  @Date    2021/6/17$ 15:08$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.audio

import android.media.*
import android.media.AudioRecord
import com.tusdk.pulse.Config
import com.tusdk.pulse.MediaInspector
import com.tusdk.pulse.audio.AudioPipe
import com.tusdk.pulse.audio.AudioProcessor
import com.tusdk.pulse.audio.AudioSamples
import com.tusdk.pulse.audio.processors.AudioPitchProcessor
import com.tusdk.pulse.audio.processors.AudioStretchProcessor
import com.tusdk.pulse.filter.FileExporter
import com.tusdk.pulse.filter.FileRecordAudioMixer
import org.lasque.tubeautysetting.AudioConvert
import org.lasque.tubeautysetting.PipeMediator
import org.lasque.tusdkpulse.core.utils.TLog
import org.lasque.tusdkpulse.core.utils.ThreadHelper
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

/**
 * TuSDK
 * org.lasque.effectcamerademo.audio
 * android-ec-demo
 *
 * @author        H.ys
 * @Date        2021/6/17  15:08
 * @Copyright    (c) 2020 tusdk.com. All rights reserved.
 *
 */
class AudioRecord(pipe: PipeMediator) {
    private val mRecordRunnable = Runnable {
        if (isOpenMic) {
            if (mAudioRecord == null) return@Runnable

            val record = mAudioRecord!!
            var lastTime = System.currentTimeMillis()
            while (mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING && !ThreadHelper.isInterrupted()) {
                val buffer = ByteArray(4096)
                val total = read(record, buffer)
                val currentAudioTime = System.currentTimeMillis() - mRecordingStart
                if (total < 0){
                    continue;
                }
                if (total < 4096) {
                    buffer.fill(0, total, 4096)
                }
                mPipeMediator.sendAudioBuffer(buffer, 4096, currentAudioTime)
            }
        } else {
            val frameDuration = 1000 * (1024.0 / 44100)
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)
            Arrays.fill(buffer, 0)
            while (!isStopRecord && !ThreadHelper.isInterrupted()) {
                val startTime = System.currentTimeMillis();
                val currentAudioTime = System.currentTimeMillis() - mRecordingStart
                mPipeMediator.sendAudioBuffer(buffer, bufferSize, currentAudioTime)
                val endTime = System.currentTimeMillis() - startTime
                val sleepTime = (frameDuration - endTime).toLong()
                ThreadHelper.sleep((sleepTime).toLong())
            }
        }


        mPipeMediator.sendAudioBuffer(ByteArray(4096),-1,-1)
    }

    private var mCurrentStretch = 1

    private var allAudioDuration = 0L

    private var mRecordThread: Thread = Thread(mRecordRunnable)

    private var mAudioRecord: AudioRecord? = null

    private var mBlockByteLength = 0

    private var mBufferSize = 0

    private var mRecordingStart = 0L

    private var isNeedMixer = false

    private var isOpenMic = true

    private var isStopRecord = false;

    private var mAudioStretch = 1.0

    private var mMinBufferSize = 0

    // ------------------ AudioPipe -------------------------------

    private val mPipeMediator: PipeMediator = pipe;

    public fun updateMicState(isOpen: Boolean) {
        isOpenMic = isOpen
    }

    public fun startRecord() {
        if (initRecorder()) {
            if (mRecordingStart == 0L) mRecordingStart = System.currentTimeMillis()
            if (isOpenMic) {
                mAudioRecord!!.startRecording()
            } else {
                isStopRecord = false
            }
            mRecordThread = ThreadHelper.runThread(mRecordRunnable)
        }

    }

    public fun stopRecord() {
        if (isOpenMic) {
            if (mAudioRecord == null || mAudioRecord!!.state != AudioRecord.STATE_INITIALIZED) return

            try {
                mAudioRecord?.stop()
                mAudioRecord?.release()
            } catch (e: Exception) {

            }
        } else {
            isStopRecord = true
            TLog.e("stop record --- 1")
        }
        mRecordThread.interrupt();
    }

    public fun release() {
        try {
            stopRecord()
        } catch (e: Exception) {

        }
    }

    private fun read(audioRecord: AudioRecord, bys: ByteArray): Int {
        var result = 0
        try {
            result = audioRecord.read(bys, 0, 4096)
        } catch (e : Exception) {
        }
        if (result < 0) {
        }
        return result
    }

    private fun initRecorder(): Boolean {
        val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_STEREO
        val audioBitWidth = AudioFormat.ENCODING_PCM_16BIT
        val channelCount = 2
        val bitWidth = 16

        mBlockByteLength = 1024 * channelCount * (bitWidth / 8)

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioBitWidth)
        mMinBufferSize = minBufferSize
        val inputBufferSize = minBufferSize * 4
        val frameSizeInBytes = channelCount * 2
        mBufferSize = inputBufferSize / frameSizeInBytes * frameSizeInBytes
        TLog.e("[Debug] current record info minBufferSize %s inputBufferSize %s frameSize %s mbufferSize %s",minBufferSize,inputBufferSize,frameSizeInBytes,mBufferSize)

        if (mBufferSize < 1) {
            return false
        }



        mAudioRecord =
            AudioRecord(audioSource, sampleRate, channelConfig, audioBitWidth, 4096)

        return mAudioRecord!!.state == AudioRecord.STATE_INITIALIZED

    }

}
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
import com.tusdk.pulse.filter.FileExporter
import com.tusdk.pulse.filter.FileRecordAudioMixer
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
class AudioRecord {

    data class AudioItem(var buffer : ByteArray,val length : Int,val time : Long) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AudioItem) return false

            if (!buffer.contentEquals(other.buffer)) return false
            if (length != other.length) return false
            if (time != other.time) return false

            return true
        }

        override fun hashCode(): Int {
            var result = buffer.contentHashCode()
            result = 31 * result + length
            result = 31 * result + time.hashCode()
            return result
        }
    }
    private val mOutputQueue : LinkedBlockingQueue<AudioItem> = LinkedBlockingQueue(Int.MAX_VALUE)

    private val mRecordRunnable = Runnable {
        if (isOpenMic){
            if (mAudioRecord == null) return@Runnable

            val record = mAudioRecord!!
            var lastTime = System.currentTimeMillis()
            while (mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING){
                val buffer = ByteArray(mBufferSize)
                val total = read(record,buffer)
                val currentAudioTime = System.currentTimeMillis() - mRecordingStart
                val audioItem = AudioItem(buffer,total,currentAudioTime)
                if (isNeedMixer){
                    val res = mAudioMixer?.sendPrimaryAudio(audioItem.buffer,audioItem.length)
                    if (res == -4){
                        break;
                    }
                } else {
                    mOutputQueue.put(audioItem)
                }
            }
        } else {
            val frameDuration = 1000 * (1024.0 / 44100)
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)
            Arrays.fill(buffer,0)
            while (!isStopRecord){
                val startTime = System.currentTimeMillis();
                val currentAudioTime = System.currentTimeMillis() - mRecordingStart
                val audioItem = AudioItem(buffer,bufferSize,currentAudioTime)
                if (isNeedMixer){
                    val res = mAudioMixer?.sendPrimaryAudio(audioItem.buffer,audioItem.length)
                    if (res == -4){
                        break
                    }
                } else {
                    mOutputQueue.put(audioItem)

                }
                val endTime = System.currentTimeMillis() - startTime
                val sleepTime = (frameDuration - endTime).toLong()
                ThreadHelper.sleep((sleepTime).toLong())
            }
        }


    }

    private var mCurrentStretch = 1

    private var allAudioDuration = 0L

    private var mRecordThread : Thread = Thread(mRecordRunnable)

    private var mAudioRecord : AudioRecord? = null

    private var mBlockByteLength = 0

    private var mBufferSize = 0

    private var mRecordingStart = 0L

    private var mAudioMixer : FileRecordAudioMixer? = null

    private var isNeedMixer =  false

    private var isOpenMic = true

    private var isStopRecord = false;

    private var mAudioStretch = 1.0

    private var mMixerRunnable = Runnable {
        while (mAudioMixer != null){
            val buffer = ByteArray(mBufferSize)
            var res = -2
            res = mAudioMixer!!.getPCMForRecord(buffer,mBufferSize)
            if (res < 0){
                if (res == -2) break
                else continue
            } else {
                val currentAudioTime = System.currentTimeMillis() - mRecordingStart
                val audioItem = AudioItem(buffer,mBufferSize,currentAudioTime)
                mOutputQueue.put(audioItem)
            }
        }
    }

    private var mMixerThread : Thread = Thread(mMixerRunnable)

    private var mFileRecorder : FileExporter? = null

    private val mFileRunnable = Runnable {
        while (!Thread.currentThread().isInterrupted){
            val item = mOutputQueue.poll()
            if (item != null){
                mFileRecorder?.sendAudioData(item.buffer,item.length,item.time)

                val currentAudioTime = item.time * mCurrentStretch

                allAudioDuration += currentAudioTime
            }
        }
    }

    private var mWriterThread : Thread = Thread(mFileRunnable)

    private fun read(record: AudioRecord, bys: ByteArray): Int {
        var result = 0
        try {
            result = record.read(bys, 0, mBlockByteLength)
        } catch (e: Exception) {
        }
        if (result < 0) {
        }
        return result
    }

    public fun updateMicState(isOpen : Boolean){
        isOpenMic = isOpen
    }

    public fun updateAudioStretch(audioStretch : Double){
        mAudioStretch = audioStretch
    }

    public fun startRecord(fileExporter: FileExporter){
        mFileRecorder = fileExporter
        if (initRecorder()){
            if (mRecordingStart == 0L) mRecordingStart = System.currentTimeMillis()
            if (isOpenMic){
                mAudioRecord!!.startRecording()
            } else {
                isStopRecord = false
            }
            mRecordThread = ThreadHelper.runThread(mRecordRunnable)
            if (isNeedMixer){
                mMixerThread = ThreadHelper.runThread(mMixerRunnable)
            }
            mWriterThread = ThreadHelper.runThread(mFileRunnable)
        }

    }

    public fun stopRecord(){
        if (isOpenMic){
            if (mAudioRecord == null || mAudioRecord!!.state != AudioRecord.STATE_INITIALIZED) return

            try {
                mAudioRecord?.stop()
                mAudioRecord?.release()
            } catch (e : Exception){

            }
        } else {
            isStopRecord = true
            TLog.e("stop record --- 1")
        }

        mRecordThread.interrupt();
        mMixerThread.interrupt();
        mWriterThread.interrupt();

    }

    public fun release(){
        try {
            stopRecord()
        } catch (e : Exception){

        }
    }

    private fun initRecorder() : Boolean{
        val audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_STEREO
        val audioBitWidth = AudioFormat.ENCODING_PCM_16BIT
        val channelCount = 2
        val bitWidth = 16

        mBlockByteLength = 1024 * channelCount * (bitWidth / 8)

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioBitWidth)
        val inputBufferSize = minBufferSize * 4
        val frameSizeInBytes = channelCount * 2
        mBufferSize = inputBufferSize / frameSizeInBytes * frameSizeInBytes

        if (mBufferSize < 1) {
            return false
        }

        mAudioRecord =
            AudioRecord(audioSource, sampleRate, channelConfig, audioBitWidth, mBufferSize)

        return mAudioRecord!!.state == AudioRecord.STATE_INITIALIZED

    }

    public fun initAudioMixer(path : String,startPos : Long){
        val inspector = MediaInspector.shared()
        val info = inspector.inspect(path)

        for (stream in info.streams){
            if (stream is MediaInspector.MediaInfo.Audio){
                val audioItem = AudioMixerItem(path,stream.bitrate,stream.channels,stream.sampleRate)
                isNeedMixer = true

                mAudioMixer = FileRecordAudioMixer()
                val config = Config()
                config.setString(FileRecordAudioMixer.CONFIG_PATH,path)
                config.setNumber(FileRecordAudioMixer.CONFIG_CHANNELS,2)
                config.setNumber(FileRecordAudioMixer.CONFIG_SAMPLE_RATE,44100)
                config.setNumber(FileRecordAudioMixer.CONFIG_AUDIO_STRETCH,mAudioStretch)
                config.setNumber(FileRecordAudioMixer.CONFIG_FILE_MIX_WEIGHT,0.5);
                if (startPos > 0){
                    config.setNumber(FileRecordAudioMixer.CONFIG_START_POS,startPos)
                }
                val res = mAudioMixer!!.open(config)
                TLog.e("audio mixer state ${res}")

                ThreadHelper.runThread {
                    val channels =
                        AudioFormat.CHANNEL_OUT_STEREO

                    val bufferSize = AudioTrack.getMinBufferSize(
                        44100,
                        channels,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    val audioTrack = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        44100,
                        channels,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                    audioTrack.setVolume(1f)
                    audioTrack.play()

                    val buffer = ByteArray(bufferSize)

                    while (true){
                        var res = -2
                        res = if (mAudioMixer != null) {mAudioMixer!!.getPCMForPlay(buffer,bufferSize)} else {res}
                        if (res < 0){
                            if (res == -2){
                                break
                            } else {
                                continue
                            }
                        } else {
                            val res = audioTrack.write(buffer,0,bufferSize)
                        }
                    }

                    audioTrack.stop()
                    audioTrack.release()
                }
                break
            }
        }

    }

    public fun resetAudioMixer(){
        if (isNeedMixer){
            mAudioMixer?.close()
            mAudioMixer = null
            isNeedMixer = false
        }
    }

}
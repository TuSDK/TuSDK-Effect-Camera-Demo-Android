/**
 *  TuSDK
 *  android-ec-demo$
 *  org.lasque.effectcamerademo.audio$
 *  @author  H.ys
 *  @Date    2021/6/15$ 16:32$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.audio

import android.media.MediaPlayer
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.audio_list_activity.*
import org.lasque.effectcamerademo.MovieRecordFullScreenActivity
import org.lasque.effectcamerademo.R
import org.lasque.effectcamerademo.base.BaseActivity

/**
 * TuSDK
 * org.lasque.effectcamerademo.audio
 * android-ec-demo
 *
 * @author        H.ys
 * @Date        2021/6/15  16:32
 * @Copyright    (c) 2020 tusdk.com. All rights reserved.
 *
 */
class AudioListActivity : BaseActivity() {

    private var mAudioAdapter : AudioAdapter? = null

    private var mMediaPlayer : MediaPlayer = MediaPlayer()

    private var mAudioPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_list_activity)
        initView()

    }

    private fun initView() {
        val audioList = AudioItemFactory.getAudioItemList(this)

        val audioAdapter = AudioAdapter(audioList.toMutableList(),this)
        audioAdapter.audioItemClickListener = object :
            AudioAdapter.OnAudioItemClickListener{
            override fun onItemPlay(item: AudioItem) {
                try{
                    if (item.path.isNotEmpty()){
                        mAudioPath = item.path
                        mMediaPlayer.release()
                        mMediaPlayer = MediaPlayer()
                        mMediaPlayer.setDataSource(item.path)
                        mMediaPlayer.isLooping = true
                        mMediaPlayer.prepare()
                        mMediaPlayer.start()
                    }
                } catch (e : Exception){

                }
            }

            override fun onItemSelect(item: AudioItem) {
                mMediaPlayer.stop()
                val intent = intent
                val bundle = Bundle()
                intent.setClass(this@AudioListActivity,MovieRecordFullScreenActivity::class.java)
                bundle.putSerializable("audioPath",item)
                intent.putExtras(bundle)
                setResult(11,intent)
                finish()
            }

        }

        mAudioAdapter = audioAdapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        lsq_audio_list.layoutManager = layoutManager
        lsq_audio_list.adapter = audioAdapter

        lsq_audio_list_close.setOnClickListener {
            finish()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
    }


}
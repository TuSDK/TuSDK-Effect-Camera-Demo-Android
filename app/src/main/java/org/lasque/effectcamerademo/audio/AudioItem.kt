package org.lasque.effectcamerademo.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import com.tusdk.pulse.utils.AssetsMapper
import java.io.Serializable
import java.util.*

/**
 * TuSDK
 * org.lasque.effectcamerademo.audio
 * android-ec-demo
 *
 * @author        H.ys
 * @Date        2021/6/15  17:09
 * @Copyright    (c) 2020 tusdk.com. All rights reserved.
 *
 */

data class AudioMixerItem(val path : String,val sampleCount : Int,val channels : Int, val sampleRate : Int){
    override fun toString(): String {
        return "AudioMixerItem(path='$path', sampleCount=$sampleCount, channels=$channels, sampleRate=$sampleRate)"
    }

}

data class AudioItem(val path : String,val name : String,val duration : Int) : Serializable {

    fun getDuration() : String{
        val m = String.format("%02d",duration / 1000 / 60)
        val s = String.format("%02d",duration / 1000 % 60)

        return "$m:$s"
    }
}

object AudioItemFactory{
    private val nameList : Array<String> = arrayOf("City Sunshine","Eye of Forgiveness")
    private val dirList : Array<String> = arrayOf("city_sunshine.mp3","eye_of_forgiveness.mp3")

    fun getAudioItemList(context: Context) : List<AudioItem>{
        var audioList : ArrayList<AudioItem> = java.util.ArrayList()
        val audioAssetsSP = context.getSharedPreferences("TuSDK-Audio",Context.MODE_PRIVATE)
        val assetsMapper = AssetsMapper(context)
        audioList.add(AudioItem("", "无",0))
        for (i in nameList.indices){
            var audioFilePath = ""
            if (!audioAssetsSP.contains(nameList[i])){
                audioFilePath = assetsMapper.mapAsset("audios/${dirList[i]}")
                audioAssetsSP.edit().putString(nameList[i],audioFilePath).apply()
            } else {
                audioFilePath = audioAssetsSP.getString(nameList[i],"")!!
            }
            val retriever : MediaMetadataRetriever = MediaMetadataRetriever()
            retriever.setDataSource(audioFilePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()
            audioList.add(AudioItem(audioFilePath, nameList[i],duration))
        }
        return audioList
    }
}

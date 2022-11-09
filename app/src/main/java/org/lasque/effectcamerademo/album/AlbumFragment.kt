/**
 *  TuSDK
 *  droid-sdk-eva$
 *  org.lsque.tusdkevademo$
 *  @author  H.ys
 *  @Date    2019/7/1$ 16:00$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.album

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tusdk.pulse.MediaInspector
import com.tusdk.pulse.Producer
import com.tusdk.pulse.Transcoder
import com.tusdk.pulse.VideoPreprocessor
import kotlinx.android.synthetic.main.movie_album_fragment.*
import kotlinx.android.synthetic.main.movie_album_fragment.lsq_editor_cut_load
import kotlinx.android.synthetic.main.movie_album_fragment.lsq_editor_cut_load_parogress
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.lasque.tusdkpulse.core.utils.sqllite.ImageSqlHelper
import org.lasque.tusdkpulse.core.utils.sqllite.ImageSqlInfo
import org.lasque.tusdkpulse.impl.view.widget.TuProgressHub
import org.lasque.effectcamerademo.R
import org.lasque.effectcamerademo.base.BaseActivity.Companion.ALBUM_RESULT_CODE
import org.lasque.effectcamerademo.utils.MD5Util
import org.lasque.tusdkpulse.core.TuSdk
import org.lasque.tusdkpulse.core.struct.TuSdkSize
import org.lasque.tusdkpulse.core.utils.FileHelper
import org.lasque.tusdkpulse.core.utils.StringHelper
import org.lasque.tusdkpulse.core.utils.TLog
import org.lasque.tusdkpulse.core.utils.ThreadHelper
import org.lasque.tusdkpulse.core.utils.image.BitmapHelper
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max

class AlbumFragment : Fragment(), LoadTaskDelegate {

    /* 最小视频时长(单位：ms) */
    private val MIN_VIDEO_DURATION = 3000

    /* 最大视频时长(单位：ms) */
    private val MAX_VIDEO_DURATION = 60000 * 3

    private var isAlpha = false

    private var mAlbumAdapter: AlbumAdapter? = null

    private var mSelectList: ArrayList<AlbumInfo> = ArrayList()

    private var mMaxSize = 1

    private var mMinSize = -1

    private var mPool: ExecutorService = Executors.newSingleThreadExecutor()

    private val transcoderSemaphore: Semaphore = Semaphore(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_album_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridLayoutManager = GridLayoutManager(activity, 4)
        lsq_album_list.layoutManager = gridLayoutManager
        mMaxSize = requireArguments().getInt("maxSize", 1)
        mMinSize = requireArguments().getInt("minSize",-1)
    }

    override fun onResume() {
        super.onResume()
        var loadTask = LoadAlbumTask(this)
        loadTask.execute()
    }

    private fun getAlbumList(): LinkedList<AlbumInfo> {
        var albumList = LinkedList<AlbumInfo>()
        when {
            requireArguments().getBoolean("onlyImage") -> getImageList(albumList)
            requireArguments().getBoolean("onlyVideo") -> getVideoList(albumList)
            else -> {
                getVideoList(albumList)
                getImageList(albumList)
            }
        }
        albumList.sortedByDescending { it.createDate }
        return albumList
    }

    /**
     * 将扫描的视频添加到集合中
     */
    private fun getVideoList(albumList: LinkedList<AlbumInfo>) {
        val cursor = requireActivity().contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, "date_added desc")
        while (cursor!!.moveToNext()) {
            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            val duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
            val createDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
            val width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH))
            val height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT))
            //根据时间长短加入显示列表
            if (duration in 1 until MAX_VIDEO_DURATION) {
                albumList.add(AlbumInfo(path, AlbumItemType.Video, duration, createDate, MD5Util.crypt(path), max(width,height)))
            }
        }
        cursor.close()
    }

    private fun getImageList(albumList: LinkedList<AlbumInfo>) {
        var imageList: ArrayList<ImageSqlInfo>? = ImageSqlHelper.getPhotoList(requireActivity().contentResolver, true)
        if (imageList != null)
            for (item in imageList) {
                albumList.add(AlbumInfo(item.path, AlbumItemType.Image, 0, item.createDate.timeInMillis, MD5Util.crypt(item.path),item.size.maxSide()))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    public fun doNextStep(){
        if (mMinSize > 0){
            if (mSelectList.size < mMinSize){
                toast("请至少选择${mMinSize}项")
                return
            }
        }

        mPool.execute {
            val pathList = ArrayList<String>()

            val transMap = HashMap<String,AlbumInfo>()

            var needTransCount = 0
            for (info in mSelectList){
                if (info.type == AlbumItemType.Video){
                    val path = info.path
                    val mediaInfo = MediaInspector.shared().inspect(path)
                    for (index in mediaInfo.streams){
                        if (index is MediaInspector.MediaInfo.Video){
                            if (false){

                                val pathCache = requireContext().getSharedPreferences("path-cache",Context.MODE_PRIVATE)

                                if (pathCache.contains(info.md5Key)){
                                    val cachePath = pathCache.getString(info.md5Key,"")!!
                                    val cacheFile = File(cachePath)
                                    if (cacheFile.exists()){
                                        info.path = cachePath
                                        continue
                                    } else {
                                        pathCache.edit().remove(info.md5Key).apply()
                                    }
                                }
                                transMap.put(info.path,info)
                                needTransCount ++
                            } else {
                                val toPath = getOutputTempFilePath()
                                FileHelper.copyFile(File(path),toPath)
                                info.path = toPath.absolutePath
                            }
                        }
                    }
                } else {
                    val path = info.path
                    val suffixIndex = path.lastIndexOf(".")
                    val suffix = path.substring(suffixIndex+1,path.length)
                    val toPath = getTempImagePath(suffix)
                    if (info.maxSize > 8192){
                        val oBitmap = BitmapHelper.getBitmap(File(path))
                        val saveBitmap = BitmapHelper.imageResize(oBitmap, TuSdkSize.create(2048))
                        BitmapHelper.saveBitmap(toPath,saveBitmap,100)
                    } else {
                        FileHelper.copyFile(File(path),toPath)
                    }
                    info.path = toPath.absolutePath
                }
            }

            if (needTransCount > 0){
                runOnUiThread {
                    toast("视频关键帧过少,需要进行转码")
                    (activity as AlbumActivity).setEnable(false)
                    toast("视频转码开始")
                }

                var offset = 0.0
                for (set in transMap){
                    needTransCount --
                    set.value.audioPath = set.value.path
                    set.value.path = videoTranscoder(set.key,offset, transMap.size.toDouble(),needTransCount == 0,set.value.duration)
                    val pathCache = requireContext().getSharedPreferences("path-cache",Context.MODE_PRIVATE)
                    pathCache.edit().putString(set.value.md5Key,set.value.path).apply()
                    offset += 100.0 / transMap.size
                }

                runOnUiThread {
                    Toast.makeText(requireContext(), "视频转码结束", Toast.LENGTH_SHORT).show()
                    (activity as AlbumActivity).setEnable(true)
                }

            }
//            for (info in mSelectList) {
//                if (info.type == AlbumItemType.Video){
//                    val path = info.path
//                    val mediaInfo = MediaInspector.shared().inspect(path)
//                    for (index in mediaInfo.streams){
//                        if (index is MediaInspector.MediaInfo.Video){
//                            // 目前版本暂时开始视频转码判断
//                            if (!index.directReverse){
//
//                                info.path = videoTranscoder(path)
//                            } else {
//                                val toPath = getOutputTempFilePath()
//                                FileHelper.copyFile(File(path),toPath)
//                                info.path = toPath.absolutePath
//                            }
//                            // 如需关闭视频转码 打开下面的代码,注释上方的代码
////
////                            val toPath = getOutputTempFilePath()
////                            FileHelper.copyFile(File(path),toPath)
////                            info.path = toPath.absolutePath
//                        }
//                    }
//                } else {
//
//                }
//
//                pathList.add(info.path)
//            }
            runOnUiThread {
                val intent = Intent()
                val bundle = Bundle()

                bundle.putSerializable("select",mSelectList)
                intent.putExtra("select",bundle)
//        intent.putStringArrayListExtra("select", pathList)
                activity?.setResult(ALBUM_RESULT_CODE, intent)
                activity?.finish()
            }
        }


    }



    override fun onLoadFinish(imageInfos: MutableList<AlbumInfo>) {
        if (mAlbumAdapter == null) {
            mAlbumAdapter = AlbumAdapter(requireActivity().baseContext, imageInfos, mSelectList)
            mAlbumAdapter!!.setMaxSize(mMaxSize)
            mAlbumAdapter!!.setOnItemClickListener(object : AlbumAdapter.OnItemClickListener {
                override fun onClick(view: View, item: AlbumInfo, position: Int) {
                    mAlbumAdapter!!.updateSelectedVideoPosition(position)
                    if (mMaxSize == 1) {
                        doNextStep()
                    }
                }
            })
            lsq_album_list.adapter = mAlbumAdapter
        }
        if (mAlbumAdapter!!.getAlbumList().size != imageInfos.size ||
                !(MD5Util.crypt(mAlbumAdapter!!.getAlbumList().toString()).equals(MD5Util.crypt(imageInfos.toString()))) ) {
            mAlbumAdapter!!.setAlbumList(imageInfos)
        }
    }

    override fun onLoadStart(): MutableList<AlbumInfo>? {
        return getAlbumList()
    }

    override fun onLoading() {
        TuProgressHub.showToast(activity, "数据加载中...")
    }

    /** 获取临时文件路径  */
    protected fun getOutputTempFilePath(): File {
        return File(TuSdk.getAppTempPath(), String.format("lsq_%s.mp4", StringHelper.timeStampString()))
    }

    protected fun getTempImagePath(suffix : String) : File{
        return File(TuSdk.getAppTempPath(), String.format("lsq_%s.%s", StringHelper.timeStampString(),suffix))
    }

    private fun videoTranscoder(inputPath: String,offset : Double,size : Double,isEnd : Boolean,duration : Int): String {
        val outputPath = getOutputTempFilePath().path

        val videoPreprocessor = VideoPreprocessor()
        videoPreprocessor.setListener { a, ts ->
            when(a){
                VideoPreprocessor.Action.OPEN -> {

                }
                VideoPreprocessor.Action.CLOSE -> {
//                    transcoderSemaphore.release()
                    runOnUiThread {
                        if (isEnd){
                            (activity as AlbumActivity).setEnable(true)
                            lsq_editor_cut_load.setVisibility(View.GONE)
                            lsq_editor_cut_load_parogress.setValue(0f)
                        }
                    }
                }
                VideoPreprocessor.Action.START -> {
                    runOnUiThread {
                        lsq_editor_cut_load.setVisibility(View.VISIBLE)
                    }
                }
                VideoPreprocessor.Action.CANCEL -> {

                }
                VideoPreprocessor.Action.WRITTING -> {
                    val currentDuration = duration
                    TLog.e("ts : ${ts}  currentDuration : ${currentDuration}")
                    runOnUiThread {
                        lsq_editor_cut_load_parogress.setValue((offset +( (ts / currentDuration.toFloat()) * 100f / size)).toFloat())
                    }
                }
            }
        }

        val processerConfig = VideoPreprocessor.Config()
        processerConfig.inputPath = inputPath
        processerConfig.outputPath = outputPath
        processerConfig.keyint = 1

        if(!videoPreprocessor.open(processerConfig)){
            return ""
        }
        videoPreprocessor.start()


//        val transcoder = Transcoder()
//        transcoder.setListener(object : Producer.Listener {
//            override fun onEvent(state: Producer.State?, ts: Long) {
//                if (state == Producer.State.kWRITING) {
//                    val currentDuration = transcoder.duration.toFloat()
//                    runOnUiThread {
//                        lsq_editor_cut_load.setVisibility(View.VISIBLE)
//                        lsq_editor_cut_load_parogress.setValue((offset +( (ts / currentDuration) * 100f / size)).toFloat())
//                    }
//                } else if (state == Producer.State.kEND) {
//                    transcoderSemaphore.release()
//                    runOnUiThread {
//                        if (isEnd){
//                            (activity as AlbumActivity).setEnable(true)
//                            lsq_editor_cut_load.setVisibility(View.GONE)
//                            lsq_editor_cut_load_parogress.setValue(0f)
//                        }
//                    }
//                }
//            }
//
//        })
//
//        val transcoderConfig = Producer.OutputConfig()
//        transcoderConfig.keyint = 0
//        transcoder.setOutputConfig(transcoderConfig)
//
//        if (!transcoder.init(outputPath, inputPath)) {
//            TLog.e("Transcoder Error")
//        }
//        transcoder.start()
        setCanBackPressed(false)
//        transcoderSemaphore.acquire()
        videoPreprocessor.close(true)
        return outputPath
    }

    protected fun setCanBackPressed(b : Boolean){
        (requireActivity() as AlbumActivity).setCanBackPressed(b)
    }

    companion object {

        /**
         * 相册加载
         */
        internal class LoadAlbumTask(private val loadTaskDelegate: LoadTaskDelegate) : AsyncTask<Void, Int, MutableList<AlbumInfo>>() {

            override fun doInBackground(vararg voids: Void): MutableList<AlbumInfo>? {
                return loadTaskDelegate.onLoadStart()
            }

            override fun onPreExecute() {
                loadTaskDelegate.onLoading()
                super.onPreExecute()
            }

            override fun onPostExecute(imageInfos: MutableList<AlbumInfo>?) {
                var imageInfos = imageInfos
                TuProgressHub.dismiss()
                if (imageInfos == null) imageInfos = ArrayList()
                ThreadHelper.post {
                    loadTaskDelegate.onLoadFinish(imageInfos)
                }
            }
        }
    }
}

public interface LoadTaskDelegate{
    fun onLoadFinish(imageInfos: MutableList<AlbumInfo>)

    fun onLoadStart() : MutableList<AlbumInfo>?

    fun onLoading()
}
/**
 *  TuSDK
 *  droid-sdk-eva$
 *  org.lsque.tusdkevademo$
 *  @author  H.ys
 *  @Date    2019/7/1$ 16:42$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.album

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.jetbrains.anko.find
import org.lasque.effectcamerademo.R
import org.lasque.tusdkpulse.core.view.TuSdkViewHelper

class AlbumAdapter(context: Context, albumList: MutableList<AlbumInfo>,selectList : MutableList<AlbumInfo>) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private val mContext: Context = context
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mAlbumList = albumList
    private var mItemClickListener: OnItemClickListener? = null
    private var mSelectList = selectList
    private var mSelectMap : HashMap<String,AlbumInfo> = HashMap()

    private var mSelectedPosition = -1

    private var mLastSelectPosition = -1

    private var mSelectMax = 1


    override fun getItemCount(): Int {
        return mAlbumList.size
    }

    public fun getAlbumList(): MutableList<AlbumInfo> {
        return mAlbumList;
    }

    public fun setAlbumList(albumList: MutableList<AlbumInfo>) {
        mAlbumList = albumList
        notifyDataSetChanged()
    }

    public fun setMaxSize(max : Int){
        mSelectMax = max
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = mInflater.inflate(R.layout.lsq_album_select_video_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = mAlbumList[position]
        when (currentItem.type) {
            AlbumItemType.Image -> {
                viewHolder!!.textView.visibility = View.GONE
            }
            AlbumItemType.Video -> {
                viewHolder!!.textView.visibility = View.VISIBLE
                viewHolder.textView.text = String.format("%02d:%02d", currentItem.duration / 1000 / 60, currentItem.duration / 1000 % 60)
            }
        }
        Glide.with(mContext).load(currentItem.path).into(viewHolder.imageView)
        viewHolder.itemView.setOnClickListener(object : TuSdkViewHelper.OnSafeClickListener(300) {
            override fun onSafeClick(v: View?) {
                mItemClickListener!!.onClick(viewHolder.itemView, currentItem, position)
            }
        })

        val index = mSelectList.indexOf(mAlbumList[position])
        if (index != -1){
            viewHolder.selView.text = (index+1).toString()
            viewHolder.selView.visibility = View.VISIBLE
        } else {
            viewHolder.selView.visibility = View.GONE
        }
    }


    public fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mItemClickListener = onItemClickListener
    }

    public fun updateSelectedVideoPosition(position: Int){
        if (mAlbumList.isNotEmpty() && position >=0){
            var info = mAlbumList[position]
            if (mSelectMax == 1){
                if (mSelectedPosition == position) return

                mLastSelectPosition = mSelectedPosition
                mSelectedPosition = position

                if (mSelectMap[info.md5Key] != null) {
                    mSelectList.remove(info)
                    mSelectMap.remove(info.md5Key)
                }
                else {
                    mSelectList.clear()
                    mSelectList.add(info)
                    mSelectMap[info.md5Key] = info
                }
                notifyItemChanged(mLastSelectPosition)
                notifyItemChanged(mSelectedPosition)
            } else {
                val info = mAlbumList[position]
                if (mSelectMap[info.md5Key] != null){
                    mSelectList.remove(info)
                    mSelectMap.remove(info.md5Key)
                } else {
                    if (mSelectList.size >= mSelectMax && mSelectMax != -1){
                      return
                    } else {
                        mSelectMap[info.md5Key] = info
                        mSelectList.add(info)
                    }
                }
                notifyItemChanged(position)

                for (info in mSelectList){
                    notifyItemChanged(mAlbumList.indexOf(info))
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.find(R.id.lsq_video_thumb_view)
        val textView: TextView = itemView.find(R.id.lsq_movie_time)
        val selView : TextView = itemView.find(R.id.lsq_video_sel)
    }

    interface OnItemClickListener {
        fun onClick(view: View, item: AlbumInfo, position: Int)
    }
}
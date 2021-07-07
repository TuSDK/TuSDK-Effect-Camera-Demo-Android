package org.lasque.effectcamerademo.base

import androidx.recyclerview.widget.RecyclerView

/**
 * TuSDK
 * org.lasque.tusdkeditoreasydemo.base
 * PulseDemo
 *
 * @author        H.ys
 * @Date        2020/10/22  11:54
 * @Copyright    (c) 2020 tusdk.com. All rights reserved.
 *
 */
interface OnItemClickListener<I,H : RecyclerView.ViewHolder> {
    fun onItemClick(pos: Int, holder: H, item: I)
}

interface OnItemDeleteClickListener<I,H : RecyclerView.ViewHolder>{
    fun onItemDelete(pos: Int, holder: H, item: I)
}
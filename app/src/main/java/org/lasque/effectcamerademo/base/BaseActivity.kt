/**
 *  TuSDK
 *  PulseDemo$
 *  org.lasque.effectcamerademo.base$
 *  @author  H.ys
 *  @Date    2020/10/22$ 16:08$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import org.jetbrains.anko.startActivityForResult
import org.lasque.effectcamerademo.album.AlbumActivity

/**
 * TuSDK
 * org.lasque.effectcamerademo.base
 * PulseDemo
 *
 * @author        H.ys
 * @Date        2020/10/22  16:08
 * @Copyright    (c) 2020 tusdk.com. All rights reserved.
 *
 */
open class BaseActivity : FragmentActivity() {

    companion object{
        public const val ALBUM_RESULT_CODE = 1
        public const val ALBUM_REQUEST_CODE = 1
    }

    protected var mDisableAllClick = false

    protected var mCanBackPressed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
    }

    public fun openAlbum(max: Int, onlyImage: Boolean, onlyVideo: Boolean,min : Int = -1) {
        startActivityForResult<AlbumActivity>(ALBUM_REQUEST_CODE, "maxSize" to max, "onlyImage" to onlyImage, "onlyVideo" to onlyVideo,"minSize" to min)
    }

    public fun openAlbum(max: Int, onlyImage: Boolean, onlyVideo: Boolean,requestCode : Int,min : Int = -1){
        startActivityForResult<AlbumActivity>(requestCode, "maxSize" to max, "onlyImage" to onlyImage, "onlyVideo" to onlyVideo,"minSize" to min)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    public fun setEnable(b : Boolean){
        mDisableAllClick = !b
    }

    public fun setCanBackPressed(b : Boolean){
        mCanBackPressed = b
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!mDisableAllClick){
            super.dispatchTouchEvent(ev)
        } else {
            mDisableAllClick
        }
    }

    override fun onBackPressed() {
        if (mCanBackPressed){
            super.onBackPressed()
        }
    }
}
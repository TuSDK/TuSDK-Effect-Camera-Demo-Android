/**
 *  TuSDK
 *  droid-sdk-eva$
 *  org.lsque.tusdkevademo$
 *  @author  H.ys
 *  @Date    2019/7/1$ 16:05$
 *  @Copyright    (c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.effectcamerademo.album

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class AlbumInfo(var path :String,var type : AlbumItemType,var duration : Int,var createDate : Long,val md5Key : String,val maxSize : Int = 0,var audioPath : String = path) : Serializable

enum class AlbumItemType : Serializable{
    Image,Video;
}